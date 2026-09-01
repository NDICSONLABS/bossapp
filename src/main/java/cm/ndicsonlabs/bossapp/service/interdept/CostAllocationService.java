// src/main/java/com/institution/finance/service/CostAllocationService.java
package cm.ndicsonlabs.bossapp.service.interdept;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.interdept.*;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRuleRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRuleTargetRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRunLineRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.CostAllocationRunRepository;
import cm.ndicsonlabs.bossapp.service.AuditService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CostAllocationService {

    private final CostAllocationRuleRepository ruleRepository;
    private final CostAllocationRuleTargetRepository targetRepository;
    private final CostAllocationRunRepository runRepository;
    private final CostAllocationRunLineRepository runLineRepository;
    private final DepartmentRepository departmentRepository;
    private final InternalBillingAccountingService accountingService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public CostAllocationService(
            CostAllocationRuleRepository ruleRepository,
            CostAllocationRuleTargetRepository targetRepository,
            CostAllocationRunRepository runRepository,
            CostAllocationRunLineRepository runLineRepository,
            DepartmentRepository departmentRepository,
            InternalBillingAccountingService accountingService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.ruleRepository = ruleRepository;
        this.targetRepository = targetRepository;
        this.runRepository = runRepository;
        this.runLineRepository = runLineRepository;
        this.departmentRepository = departmentRepository;
        this.accountingService = accountingService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public CostAllocationRule createRule(UUID sourceDepartmentId, String name, String description) {
        requireInternalBillingPrivilege();

        Department source = departmentRepository.findById(sourceDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Source department not found"));

        CostAllocationRule rule = new CostAllocationRule();
        rule.setName(name);
        rule.setSourceDepartment(source);
        rule.setDescription(description);
        rule.setActive(true);

        ruleRepository.save(rule);

        auditService.log(
                "COST_ALLOCATION_RULE",
                rule.getId(),
                "CREATE_RULE",
                null,
                name,
                "Cost allocation rule created"
        );

        return rule;
    }

    @Transactional
    public CostAllocationRuleTarget addTarget(UUID ruleId, UUID receiverDepartmentId, BigDecimal percentage) {
        requireInternalBillingPrivilege();

        CostAllocationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Cost allocation rule not found"));

        Department receiver = departmentRepository.findById(receiverDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver department not found"));

        if (receiver.getId().equals(rule.getSourceDepartment().getId())) {
            throw new IllegalArgumentException("Receiver department cannot be the same as source department.");
        }

        if (percentage == null || percentage.signum() <= 0) {
            throw new IllegalArgumentException("Allocation percentage must be greater than zero.");
        }

        CostAllocationRuleTarget target = new CostAllocationRuleTarget();
        target.setRule(rule);
        target.setReceiverDepartment(receiver);
        target.setPercentage(percentage);

        targetRepository.save(target);

        auditService.log(
                "COST_ALLOCATION_RULE",
                rule.getId(),
                "ADD_TARGET",
                null,
                receiver.getName(),
                "Allocation target added"
        );

        return target;
    }

    @Transactional
    public CostAllocationRun runAllocation(UUID ruleId, int year, int month, BigDecimal totalAmount) {
        requireInternalBillingPrivilege();

        CostAllocationRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Cost allocation rule not found"));

        if (!rule.isActive()) {
            throw new IllegalStateException("Cost allocation rule is inactive.");
        }

        if (runRepository.existsByRuleIdAndPeriodYearAndPeriodMonth(ruleId, year, month)) {
            throw new IllegalStateException("Allocation has already been run for this rule and period.");
        }

        if (totalAmount == null || totalAmount.signum() <= 0) {
            throw new IllegalArgumentException("Allocation amount must be greater than zero.");
        }

        List<CostAllocationRuleTarget> targets = targetRepository.findByRuleId(ruleId);

        if (targets.isEmpty()) {
            throw new IllegalStateException("Cost allocation rule has no target departments.");
        }

        BigDecimal totalPercentage = targets.stream()
                .map(CostAllocationRuleTarget::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalStateException("Target percentages must total exactly 100%.");
        }

        CostAllocationRun run = new CostAllocationRun();
        run.setRule(rule);
        run.setPeriodYear(year);
        run.setPeriodMonth(month);
        run.setTotalAmount(totalAmount);
        run.setStatus("POSTED");
        run.setPostedBy(currentUserService.username());

        runRepository.save(run);

        List<CostAllocationRunLine> lines = new ArrayList<>();
        BigDecimal allocatedSum = BigDecimal.ZERO;

        for (CostAllocationRuleTarget target : targets) {
            BigDecimal amount = totalAmount
                    .multiply(target.getPercentage())
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            CostAllocationRunLine line = new CostAllocationRunLine();
            line.setRun(run);
            line.setReceiverDepartment(target.getReceiverDepartment());
            line.setPercentage(target.getPercentage());
            line.setAmount(amount);

            lines.add(line);
            allocatedSum = allocatedSum.add(amount);
        }

        BigDecimal roundingDifference = totalAmount.subtract(allocatedSum);

        if (roundingDifference.signum() != 0 && !lines.isEmpty()) {
            CostAllocationRunLine first = lines.get(0);
            first.setAmount(first.getAmount().add(roundingDifference));
        }

        runLineRepository.saveAll(lines);

        LocalDate allocationDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1);

        accountingService.postCostAllocation(run, lines, rule.getSourceDepartment(), allocationDate);

        auditService.log(
                "COST_ALLOCATION_RUN",
                run.getId(),
                "RUN_ALLOCATION",
                null,
                totalAmount.toPlainString(),
                "Cost allocation executed"
        );

        return run;
    }

    private void requireInternalBillingPrivilege() {
        if (!currentUserService.hasPrivilege("INTERNAL_BILLING_MANAGE")) {
            throw new AccessDeniedException("Current user does not have internal billing privilege.");
        }
    }
}