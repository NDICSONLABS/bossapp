// src/main/java/com/institution/finance/service/PeriodCloseService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.GlReconciliation;
import cm.ndicsonlabs.bossapp.domain.PeriodCloseTask;
import cm.ndicsonlabs.bossapp.domain.PeriodCloseValidation;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.CashierSessionRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentSubmissionRepository;
import cm.ndicsonlabs.bossapp.repository.GlReconciliationRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryBalanceRepository;
import cm.ndicsonlabs.bossapp.repository.PeriodCloseTaskRepository;
import cm.ndicsonlabs.bossapp.repository.PeriodCloseValidationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PeriodCloseService {

    private final AccountingPeriodRepository periodRepository;
    private final PeriodCloseTaskRepository taskRepository;
    private final PeriodCloseValidationRepository validationRepository;
    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final DepartmentSubmissionRepository submissionRepository;
    private final GlIntegrationService glIntegrationService;
    private final GlReconciliationRepository glReconciliationRepository;
    private final CashierSessionRepository cashierSessionRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final InventoryService inventoryService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public PeriodCloseService(
            AccountingPeriodRepository periodRepository,
            PeriodCloseTaskRepository taskRepository,
            PeriodCloseValidationRepository validationRepository,
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            DepartmentSubmissionRepository submissionRepository,
            GlIntegrationService glIntegrationService,
            GlReconciliationRepository glReconciliationRepository,
            CashierSessionRepository cashierSessionRepository,
            InventoryBalanceRepository inventoryBalanceRepository,
            InventoryService inventoryService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.periodRepository = periodRepository;
        this.taskRepository = taskRepository;
        this.validationRepository = validationRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.submissionRepository = submissionRepository;
        this.glIntegrationService = glIntegrationService;
        this.glReconciliationRepository = glReconciliationRepository;
        this.cashierSessionRepository = cashierSessionRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.inventoryService = inventoryService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public List<PeriodCloseTask> generateChecklist(UUID periodId) {
        AccountingPeriod period = getPeriod(periodId);

        createTaskIfAbsent(period, "CONFIRM_DEPARTMENT_SUBMISSIONS", "Confirm all department submissions are accepted, rejected, or completed.", true);
        createTaskIfAbsent(period, "VALIDATE_TRIAL_BALANCE", "Validate that general ledger debits equal credits.", true);
        createTaskIfAbsent(period, "RECONCILE_SUBLEDGERS", "Run and review sub-ledger to GL reconciliations.", true);
        createTaskIfAbsent(period, "REVIEW_UNPOSTED_TRANSACTIONS", "Review and post all remaining operational transactions.", true);
        createTaskIfAbsent(period, "REVIEW_CASHIER_SESSIONS", "Review and approve daily cashier sessions.", true);
        createTaskIfAbsent(period, "REVIEW_INVENTORY_VALUATION", "Review inventory valuation and negative stock exceptions.", true);
        createTaskIfAbsent(period, "GENERATE_FINANCIAL_STATEMENTS", "Generate formal financial statements.", true);
        createTaskIfAbsent(period, "FINAL_REVIEW", "Final central accounting review.", true);

        return taskRepository.findByPeriodIdOrderByCreatedAtAsc(periodId);
    }

    @Transactional
    public List<PeriodCloseValidation> runValidations(UUID periodId) {
        AccountingPeriod period = getPeriod(periodId);

        List<PeriodCloseValidation> validations = new ArrayList<>();

        validations.add(validateTrialBalance(period));
        validations.add(validateUnpostedTransactions(period));
        validations.add(validateDepartmentSubmissions(period));
        validations.add(validateGlReconciliations(period));
        validations.add(validateCashierSessions(period));
        validations.add(validateInventory(period));

        return validationRepository.saveAll(validations);
    }

    @Transactional
    public PeriodCloseTask completeTask(UUID periodId, String taskCode, String notes) {
        AccountingPeriod period = getPeriod(periodId);

        if (!"OPEN".equals(period.getStatus()) && !"SOFT_CLOSED".equals(period.getStatus())) {
            throw new IllegalStateException("Tasks can only be completed while the period is open or soft closed.");
        }

        PeriodCloseTask task = taskRepository.findByPeriodIdAndTaskCode(periodId, taskCode)
                .orElseThrow(() -> new IllegalArgumentException("Period close task not found: " + taskCode));

        if ("COMPLETED".equals(task.getStatus())) {
            throw new IllegalStateException("Task is already completed.");
        }

        enforceTaskValidations(periodId, taskCode);

        task.setStatus("COMPLETED");
        task.setCompletedBy(currentUserService.username());
        task.setCompletedAt(Instant.now());
        task.setNotes(notes);

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "COMPLETE_CLOSE_TASK",
                null,
                taskCode,
                notes
        );

        return taskRepository.save(task);
    }

    @Transactional
    public AccountingPeriod softClose(UUID periodId) {
        requirePeriodClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"OPEN".equals(period.getStatus())) {
            throw new IllegalStateException("Only open periods can be soft closed.");
        }

        validateAllRequiredTasksCompleted(periodId);
        validateNoFailedValidations(periodId);

        period.setStatus("SOFT_CLOSED");

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "SOFT_CLOSE_PERIOD",
                null,
                period.getStatus(),
                "Period soft closed"
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod close(UUID periodId) {
        requirePeriodClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"SOFT_CLOSED".equals(period.getStatus())) {
            throw new IllegalStateException("Only soft closed periods can be finally closed.");
        }

        validateAllRequiredTasksCompleted(periodId);
        validateNoFailedValidations(periodId);

        period.setStatus("CLOSED");

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "CLOSE_PERIOD",
                null,
                period.getStatus(),
                "Period finally closed"
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod lock(UUID periodId) {
        requirePeriodClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"CLOSED".equals(period.getStatus())) {
            throw new IllegalStateException("Only closed periods can be hard locked.");
        }

        boolean activeSubmissionExists = submissionRepository.existsByPeriodAndStatusIn(
                period,
                List.of(
                        "DRAFT",
                        "DEPARTMENT_APPROVED",
                        "SUBMITTED",
                        "UNDER_CENTRAL_REVIEW"
                )
        );

        if (activeSubmissionExists) {
            throw new IllegalStateException("Period cannot be locked while active submissions exist.");
        }

        long unposted = glIntegrationService.countUnpostedForPeriod(period);

        if (unposted > 0) {
            throw new IllegalStateException("Period cannot be locked because unposted transactions still exist.");
        }

        period.setStatus("LOCKED");
        period.setClosedBy(currentUserService.username());
        period.setLockedDate(LocalDate.now());

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "LOCK_PERIOD",
                null,
                period.getStatus(),
                "Period hard locked"
        );

        return periodRepository.save(period);
    }

    private PeriodCloseValidation validateTrialBalance(AccountingPeriod period) {
        List<AccountingEntry> entries = entryRepository.findByEntryDateBetweenAndStatus(
                period.getStartDate(),
                period.getEndDate(),
                "POSTED"
        );

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                totalDebit = totalDebit.add(nullSafe(line.getDebit()));
                totalCredit = totalCredit.add(nullSafe(line.getCredit()));
            }
        }

        boolean balanced = totalDebit.compareTo(totalCredit) == 0;

        return buildValidation(
                period,
                "TRIAL_BALANCE_BALANCED",
                balanced ? "PASS" : "FAIL",
                "Total debits: " + totalDebit + ", total credits: " + totalCredit
        );
    }

    private PeriodCloseValidation validateUnpostedTransactions(AccountingPeriod period) {
        long unposted = glIntegrationService.countUnpostedForPeriod(period);

        return buildValidation(
                period,
                "UNPOSTED_TRANSACTIONS",
                unposted == 0 ? "PASS" : "FAIL",
                "Unposted transaction count: " + unposted
        );
    }

    private PeriodCloseValidation validateDepartmentSubmissions(AccountingPeriod period) {
        boolean activeSubmissionExists = submissionRepository.existsByPeriodAndStatusIn(
                period,
                List.of(
                        "DRAFT",
                        "DEPARTMENT_APPROVED",
                        "SUBMITTED",
                        "UNDER_CENTRAL_REVIEW"
                )
        );

        return buildValidation(
                period,
                "DEPARTMENT_SUBMISSIONS",
                activeSubmissionExists ? "FAIL" : "PASS",
                activeSubmissionExists
                        ? "Active department submissions still exist."
                        : "No active department submissions remain."
        );
    }

    private PeriodCloseValidation validateGlReconciliations(AccountingPeriod period) {
        List<GlReconciliation> reconciliations = glReconciliationRepository
                .findByReconciliationDateOrderByCreatedAtDesc(period.getEndDate());

        if (reconciliations.isEmpty()) {
            return buildValidation(
                    period,
                    "GL_RECONCILIATION",
                    "WARN",
                    "No GL reconciliation was run for the period end date."
            );
        }

        boolean hasVariance = reconciliations.stream()
                .anyMatch(reconciliation -> "VARIANCE".equals(reconciliation.getStatus()));

        return buildValidation(
                period,
                "GL_RECONCILIATION",
                hasVariance ? "FAIL" : "PASS",
                hasVariance
                        ? "One or more GL reconciliations have variance."
                        : "All GL reconciliations for the period end date are balanced."
        );
    }

    private PeriodCloseValidation validateCashierSessions(AccountingPeriod period) {
        long unapprovedSessions = cashierSessionRepository.countBySessionDateBetweenAndStatusNot(
                period.getStartDate(),
                period.getEndDate(),
                "APPROVED"
        );

        return buildValidation(
                period,
                "CASHIER_SESSIONS",
                unapprovedSessions == 0 ? "PASS" : "WARN",
                "Unapproved cashier session count: " + unapprovedSessions
        );
    }

    private PeriodCloseValidation validateInventory(AccountingPeriod period) {
        long negativeStockCount = inventoryBalanceRepository.countByQuantityOnHandLessThan(BigDecimal.ZERO);
        BigDecimal valuation = inventoryService.valuation(null);

        if (negativeStockCount > 0) {
            return buildValidation(
                    period,
                    "INVENTORY_VALIDATION",
                    "FAIL",
                    "Negative stock balance count: " + negativeStockCount
            );
        }

        return buildValidation(
                period,
                "INVENTORY_VALIDATION",
                "PASS",
                "Inventory valuation: " + valuation
        );
    }

    private void enforceTaskValidations(UUID periodId, String taskCode) {
        switch (taskCode) {
            case "CONFIRM_DEPARTMENT_SUBMISSIONS" ->
                    requireValidation(periodId, "DEPARTMENT_SUBMISSIONS", "PASS");

            case "VALIDATE_TRIAL_BALANCE" ->
                    requireValidation(periodId, "TRIAL_BALANCE_BALANCED", "PASS");

            case "RECONCILE_SUBLEDGERS" ->
                    requireValidation(periodId, "GL_RECONCILIATION", "PASS", "WARN");

            case "REVIEW_UNPOSTED_TRANSACTIONS" ->
                    requireValidation(periodId, "UNPOSTED_TRANSACTIONS", "PASS");

            case "REVIEW_CASHIER_SESSIONS" ->
                    requireValidation(periodId, "CASHIER_SESSIONS", "PASS", "WARN");

            case "REVIEW_INVENTORY_VALUATION" ->
                    requireValidation(periodId, "INVENTORY_VALIDATION", "PASS");

            default -> {
            }
        }
    }

    private void requireValidation(UUID periodId, String validationCode, String... allowedStatuses) {
        PeriodCloseValidation validation = validationRepository
                .findTopByPeriodIdAndValidationCodeOrderByCreatedAtDesc(periodId, validationCode)
                .orElseThrow(() -> new IllegalStateException(
                        "Validation has not been run: " + validationCode
                ));

        boolean allowed = Arrays.asList(allowedStatuses).contains(validation.getStatus());

        if (!allowed) {
            throw new IllegalStateException(
                    "Validation " + validationCode + " has status " + validation.getStatus()
            );
        }
    }

    private void validateAllRequiredTasksCompleted(UUID periodId) {
        List<PeriodCloseTask> tasks = taskRepository.findByPeriodIdOrderByCreatedAtAsc(periodId);

        if (tasks.isEmpty()) {
            throw new IllegalStateException("Period close checklist has not been generated.");
        }

        boolean allCompleted = tasks.stream()
                .filter(PeriodCloseTask::isRequired)
                .allMatch(task -> "COMPLETED".equals(task.getStatus()));

        if (!allCompleted) {
            throw new IllegalStateException("All required period close tasks must be completed.");
        }
    }

    private void validateNoFailedValidations(UUID periodId) {
        List<PeriodCloseValidation> validations = validationRepository
                .findByPeriodIdOrderByCreatedAtDesc(periodId);

        Map<String, String> latest = new LinkedHashMap<>();

        for (PeriodCloseValidation validation : validations) {
            latest.putIfAbsent(validation.getValidationCode(), validation.getStatus());
        }

        boolean hasFail = latest.values()
                .stream()
                .anyMatch(status -> "FAIL".equals(status));

        if (hasFail) {
            throw new IllegalStateException("One or more period close validations have failed.");
        }
    }

    private void createTaskIfAbsent(AccountingPeriod period, String code, String description, boolean required) {
        if (taskRepository.findByPeriodIdAndTaskCode(period.getId(), code).isPresent()) {
            return;
        }

        PeriodCloseTask task = new PeriodCloseTask();
        task.setPeriod(period);
        task.setTaskCode(code);
        task.setDescription(description);
        task.setRequired(required);
        task.setStatus("OPEN");

        taskRepository.save(task);
    }

    private PeriodCloseValidation buildValidation(
            AccountingPeriod period,
            String code,
            String status,
            String message
    ) {
        PeriodCloseValidation validation = new PeriodCloseValidation();
        validation.setPeriod(period);
        validation.setValidationCode(code);
        validation.setStatus(status);
        validation.setMessage(message);

        return validation;
    }

    private AccountingPeriod getPeriod(UUID periodId) {
        return periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));
    }

    private void requirePeriodClosePrivilege() {
        if (!currentUserService.hasPrivilege("PERIOD_CLOSE")) {
            throw new AccessDeniedException("Current user does not have period close privilege.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}