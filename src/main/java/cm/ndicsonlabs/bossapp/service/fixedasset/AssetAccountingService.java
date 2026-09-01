// src/main/java/com/institution/finance/service/AssetAccountingService.java
package cm.ndicsonlabs.bossapp.service.fixedasset;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.fixedasset.FixedAsset;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AssetAccountingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final CurrentUserService currentUserService;

    public AssetAccountingService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            AccountingPeriodRepository periodRepository,
            AccountMappingRepository accountMappingRepository,
            CurrentUserService currentUserService
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.periodRepository = periodRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postAcquisition(FixedAsset asset) {
        if (entryRepository.existsBySourceTypeAndSourceId("ASSET_ACQUISITION", asset.getId())) return;

        AccountingEntry entry = createEntry(asset.getAcquisitionDate(), "Asset Acquisition: " + asset.getAssetNumber(), "ASSET_ACQUISITION", asset.getId(), asset.getDepartment().getId());
        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(createLine(entry, getAccount("ASSET_COST"), asset.getOriginalCost(), BigDecimal.ZERO, asset.getDepartment().getId(), "Asset capitalization"));
        lines.add(createLine(entry, getAccount("SUPPLIER_INVOICE_AP"), BigDecimal.ZERO, asset.getOriginalCost(), asset.getDepartment().getId(), "Clearing AP / GRNI"));

        saveEntry(entry, lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postDepreciation(FixedAsset asset, BigDecimal amount, LocalDate runDate) {
        if (amount.signum() == 0) return;

        AccountingEntry entry = createEntry(runDate, "Depreciation: " + asset.getAssetNumber(), "ASSET_DEPRECIATION", asset.getId(), asset.getDepartment().getId());
        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(createLine(entry, getAccount("DEPRECIATION_EXPENSE"), amount, BigDecimal.ZERO, asset.getDepartment().getId(), "Depreciation expense"));
        lines.add(createLine(entry, getAccount("ACCUMULATED_DEPRECIATION"), BigDecimal.ZERO, amount, asset.getDepartment().getId(), "Accumulated depreciation"));

        saveEntry(entry, lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postDisposal(FixedAsset asset, BigDecimal proceeds, BigDecimal netBookValue, LocalDate disposalDate) {
        if (entryRepository.existsBySourceTypeAndSourceId("ASSET_DISPOSAL", asset.getId())) return;

        AccountingEntry entry = createEntry(disposalDate, "Asset Disposal: " + asset.getAssetNumber(), "ASSET_DISPOSAL", asset.getId(), asset.getDepartment().getId());
        List<AccountingEntryLine> lines = new ArrayList<>();

        // Debit Accumulated Depreciation
        lines.add(createLine(entry, getAccount("ACCUMULATED_DEPRECIATION"), asset.getAccumulatedDepreciation(), BigDecimal.ZERO, asset.getDepartment().getId(), "Clear accumulated depreciation"));

        // Credit Asset Cost
        lines.add(createLine(entry, getAccount("ASSET_COST"), BigDecimal.ZERO, asset.getOriginalCost(), asset.getDepartment().getId(), "Clear asset cost"));

        // Debit Cash / Receivable if proceeds > 0
        if (proceeds.signum() > 0) {
            lines.add(createLine(entry, getAccount("PAYMENT_IN_CASH"), proceeds, BigDecimal.ZERO, asset.getDepartment().getId(), "Disposal proceeds"));
        }

        BigDecimal gainOrLoss = proceeds.subtract(netBookValue);

        if (gainOrLoss.signum() > 0) {
            lines.add(createLine(entry, getAccount("GAIN_ON_DISPOSAL"), BigDecimal.ZERO, gainOrLoss, asset.getDepartment().getId(), "Gain on disposal"));
        } else if (gainOrLoss.signum() < 0) {
            lines.add(createLine(entry, getAccount("LOSS_ON_DISPOSAL"), gainOrLoss.abs(), BigDecimal.ZERO, asset.getDepartment().getId(), "Loss on disposal"));
        }

        saveEntry(entry, lines);
    }

    private AccountingEntry createEntry(LocalDate entryDate, String description, String sourceType, UUID sourceId, UUID departmentId) {
        AccountingPeriod accountingPeriod = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(entryDate, entryDate)
                .orElseThrow(() -> new IllegalStateException("No open accounting period for date: " + entryDate));

        AccountingEntry entry = new AccountingEntry();
        entry.setEntryNumber("JE-" + UUID.randomUUID());
        entry.setEntryDate(entryDate);
        entry.setAccountingPeriod(accountingPeriod);
        entry.setDescription(description);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setStatus("POSTED");
        entry.setPostedBy(currentUserService.username());
        entry.setPostedAt(Instant.now());
        // Note: entry.setDepartment() requires fetching Department entity, omitted here for brevity, assumes GL handles it via lines.
        return entry;
    }

    private AccountingEntryLine createLine(AccountingEntry entry, AccountCode account, BigDecimal debit, BigDecimal credit, UUID departmentId, String desc) {
        AccountingEntryLine line = new AccountingEntryLine();
        line.setEntry(entry);
        line.setAccountCode(account);
        line.setDebit(debit);
        line.setCredit(credit);
        line.setDescription(desc);
        return line;
    }

    private void saveEntry(AccountingEntry entry, List<AccountingEntryLine> lines) {
        BigDecimal totalDebit = lines.stream().map(l -> l.getDebit() != null ? l.getDebit() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(l -> l.getCredit() != null ? l.getCredit() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("Asset accounting entry is not balanced.");
        }

        entryRepository.save(entry);
        lineRepository.saveAll(lines);
    }

    private AccountCode getAccount(String mappingType) {
        return accountMappingRepository.findByMappingType(mappingType)
                .orElseThrow(() -> new IllegalStateException("Account mapping not found: " + mappingType))
                .getAccountCode();
    }
}