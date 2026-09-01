// src/main/java/com/institution/finance/service/AssetManagementService.java
package cm.ndicsonlabs.bossapp.service.fixedasset;

import cm.ndicsonlabs.bossapp.domain.*;
import cm.ndicsonlabs.bossapp.domain.fixedasset.*;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import cm.ndicsonlabs.bossapp.repository.*;
import cm.ndicsonlabs.bossapp.repository.fixedasset.*;
import cm.ndicsonlabs.bossapp.repository.payroll.EmployeeRepository;
import cm.ndicsonlabs.bossapp.service.AuditService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AssetManagementService {

    private final FixedAssetRepository assetRepository;
    private final AssetCategoryRepository categoryRepository;
    private final AssetDepreciationRunRepository runRepository;
    private final AssetDepreciationLineRepository lineRepository;
    private final AssetDisposalRepository disposalRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AssetAccountingService assetAccountingService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AssetManagementService(
            FixedAssetRepository assetRepository,
            AssetCategoryRepository categoryRepository,
            AssetDepreciationRunRepository runRepository,
            AssetDepreciationLineRepository lineRepository,
            AssetDisposalRepository disposalRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            AssetAccountingService assetAccountingService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.assetRepository = assetRepository;
        this.categoryRepository = categoryRepository;
        this.runRepository = runRepository;
        this.lineRepository = lineRepository;
        this.disposalRepository = disposalRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.assetAccountingService = assetAccountingService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public FixedAsset capitalizeAsset(
            UUID categoryId, UUID departmentId, UUID custodianId,
            String description, String serialNumber, String physicalLocation,
            LocalDate acquisitionDate, BigDecimal cost
    ) {
        requireAssetPrivilege();

        AssetCategory category = categoryRepository.findById(categoryId).orElseThrow();
        Department department = departmentRepository.findById(departmentId).orElseThrow();
        Employee custodian = custodianId != null ? employeeRepository.findById(custodianId).orElse(null) : null;

        if (cost.compareTo(category.getCapitalizationThreshold()) < 0) {
            throw new IllegalArgumentException("Cost is below the capitalization threshold for this category.");
        }

        BigDecimal salvageValue = cost.multiply(category.getSalvageValuePercent())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        FixedAsset asset = new FixedAsset();
        asset.setAssetNumber("AST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        asset.setDescription(description);
        asset.setCategory(category);
        asset.setDepartment(department);
        asset.setCustodianEmployee(custodian);
        asset.setPhysicalLocation(physicalLocation);
        asset.setSerialNumber(serialNumber);
        asset.setAcquisitionDate(acquisitionDate);
        asset.setCapitalizedDate(LocalDate.now());
        asset.setOriginalCost(cost);
        asset.setSalvageValue(salvageValue);
        asset.setNetBookValue(cost);
        asset.setDepreciationMethod(category.getDepreciationMethod());
        asset.setUsefulLifeMonths(category.getUsefulLifeMonths());
        asset.setStatus("CAPITALIZED");

        assetRepository.save(asset);

        assetAccountingService.postAcquisition(asset);

        auditService.log("FIXED_ASSET", asset.getId(), "CAPITALIZE", null, asset.getAssetNumber(), "Asset capitalized");

        return asset;
    }

    @Transactional
    public AssetDepreciationRun runMonthlyDepreciation(int year, int month) {
        requireAssetPrivilege();

        if (runRepository.findByPeriodYearAndPeriodMonth(year, month).isPresent()) {
            throw new IllegalStateException("Depreciation for this period has already been run.");
        }

        List<FixedAsset> activeAssets = assetRepository.findByStatusIn(List.of("CAPITALIZED", "DEPRECIATING"));
        LocalDate runDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1); // End of month

        AssetDepreciationRun run = new AssetDepreciationRun();
        run.setRunDate(runDate);
        run.setPeriodYear(year);
        run.setPeriodMonth(month);
        run.setPostedBy(currentUserService.username());

        runRepository.save(run);

        BigDecimal totalDepreciation = BigDecimal.ZERO;

        for (FixedAsset asset : activeAssets) {
            BigDecimal monthlyDepreciation = calculateMonthlyDepreciation(asset);

            if (monthlyDepreciation.signum() > 0) {
                BigDecimal maxDepreciable = asset.getOriginalCost().subtract(asset.getSalvageValue()).subtract(asset.getAccumulatedDepreciation());
                monthlyDepreciation = monthlyDepreciation.min(maxDepreciable);

                if (monthlyDepreciation.signum() > 0) {
                    asset.setAccumulatedDepreciation(asset.getAccumulatedDepreciation().add(monthlyDepreciation));
                    asset.setNetBookValue(asset.getOriginalCost().subtract(asset.getAccumulatedDepreciation()));
                    asset.setStatus("DEPRECIATING");

                    if (asset.getNetBookValue().compareTo(asset.getSalvageValue()) <= 0) {
                        asset.setStatus("FULLY_DEPRECIATED");
                    }
                    assetRepository.save(asset);

                    AssetDepreciationLine line = new AssetDepreciationLine();
                    line.setRun(run);
                    line.setAsset(asset);
                    line.setDepartment(asset.getDepartment());
                    line.setDepreciationAmount(monthlyDepreciation);
                    lineRepository.save(line);

                    assetAccountingService.postDepreciation(asset, monthlyDepreciation, runDate);

                    totalDepreciation = totalDepreciation.add(monthlyDepreciation);
                }
            }
        }

        run.setTotalDepreciation(totalDepreciation);
        return runRepository.save(run);
    }

    @Transactional
    public AssetDisposal disposeAsset(UUID assetId, LocalDate disposalDate, String disposalType, BigDecimal proceeds, String reason) {
        requireAssetPrivilege();

        FixedAsset asset = assetRepository.findById(assetId).orElseThrow();

        if ("DISPOSED".equals(asset.getStatus()) || "WRITTEN_OFF".equals(asset.getStatus())) {
            throw new IllegalStateException("Asset has already been disposed or written off.");
        }

        BigDecimal nbv = asset.getNetBookValue();
        BigDecimal gainOrLoss = proceeds.subtract(nbv);

        AssetDisposal disposal = new AssetDisposal();
        disposal.setAsset(asset);
        disposal.setDisposalDate(disposalDate);
        disposal.setDisposalType(disposalType);
        disposal.setProceeds(proceeds);
        disposal.setNetBookValueAtDisposal(nbv);
        disposal.setGainOrLoss(gainOrLoss);
        disposal.setReason(reason);
        disposal.setApprovedBy(currentUserService.username());

        disposalRepository.save(disposal);

        assetAccountingService.postDisposal(asset, proceeds, nbv, disposalDate);

        asset.setStatus("DISPOSED".equals(disposalType) ? "DISPOSED" : "WRITTEN_OFF");
        asset.setNetBookValue(BigDecimal.ZERO);
        asset.setAccumulatedDepreciation(asset.getOriginalCost().subtract(nbv)); // Adjust accum dep to clear out
        assetRepository.save(asset);

        auditService.log("FIXED_ASSET", asset.getId(), "DISPOSE", null, disposalType, reason);

        return disposal;
    }

    private BigDecimal calculateMonthlyDepreciation(FixedAsset asset) {
        BigDecimal depreciableBase = asset.getOriginalCost().subtract(asset.getSalvageValue());
        if (depreciableBase.signum() <= 0 || asset.getUsefulLifeMonths() <= 0) return BigDecimal.ZERO;

        if ("STRAIGHT_LINE".equals(asset.getDepreciationMethod())) {
            return depreciableBase.divide(BigDecimal.valueOf(asset.getUsefulLifeMonths()), 4, RoundingMode.HALF_UP);
        }

        if ("DECLINING_BALANCE".equals(asset.getDepreciationMethod())) {
            // Double declining balance simplified for monthly
            double annualRate = 2.0 / (asset.getUsefulLifeMonths() / 12.0);
            double monthlyRate = annualRate / 12.0;
            return asset.getNetBookValue().multiply(BigDecimal.valueOf(monthlyRate)).setScale(4, RoundingMode.HALF_UP);
        }

        return BigDecimal.ZERO;
    }

    private void requireAssetPrivilege() {
        if (!currentUserService.hasPrivilege("ASSET_MANAGE")) {
            throw new AccessDeniedException("Current user does not have fixed asset management privilege.");
        }
    }
}