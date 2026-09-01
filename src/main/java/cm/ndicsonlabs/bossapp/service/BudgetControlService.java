// src/main/java/com/institution/finance/service/BudgetControlService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.BudgetAdjustment;
import cm.ndicsonlabs.bossapp.domain.BudgetHeader;
import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.BudgetAdjustmentRepository;
import cm.ndicsonlabs.bossapp.repository.BudgetHeaderRepository;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.FundRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class BudgetControlService {

    private final BudgetHeaderRepository budgetHeaderRepository;
    private final BudgetLineRepository budgetLineRepository;
    private final BudgetAdjustmentRepository budgetAdjustmentRepository;
    private final FundRepository fundRepository;
    private final GrantAwardRepository grantAwardRepository;
    private final DepartmentRepository departmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public BudgetControlService(
            BudgetHeaderRepository budgetHeaderRepository,
            BudgetLineRepository budgetLineRepository,
            BudgetAdjustmentRepository budgetAdjustmentRepository,
            FundRepository fundRepository,
            GrantAwardRepository grantAwardRepository,
            DepartmentRepository departmentRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.budgetHeaderRepository = budgetHeaderRepository;
        this.budgetLineRepository = budgetLineRepository;
        this.budgetAdjustmentRepository = budgetAdjustmentRepository;
        this.fundRepository = fundRepository;
        this.grantAwardRepository = grantAwardRepository;
        this.departmentRepository = departmentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public BudgetHeader createHeader(
            Integer fiscalYear,
            UUID fundId,
            UUID grantAwardId,
            UUID departmentId,
            String description
    ) {
        requireBudgetPrivilege();

        Fund fund = fundRepository.findById(fundId)
                .orElseThrow(() -> new IllegalArgumentException("Fund not found"));

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        GrantAward grant = null;

        if (grantAwardId != null) {
            grant = grantAwardRepository.findById(grantAwardId)
                    .orElseThrow(() -> new IllegalArgumentException("Grant not found"));
        }

        BudgetHeader header = new BudgetHeader();
        header.setFiscalYear(fiscalYear);
        header.setFund(fund);
        header.setGrantAward(grant);
        header.setDepartment(department);
        header.setDescription(description);
        header.setStatus("DRAFT");
        header.setTotalAmount(BigDecimal.ZERO);
        header.setCreatedBy(currentUserService.username());

        budgetHeaderRepository.save(header);

        auditService.log(
                "BUDGET_HEADER",
                header.getId(),
                "CREATE_BUDGET",
                null,
                header.getStatus(),
                description
        );

        return header;
    }

    @Transactional
    public BudgetLine addLine(
            UUID headerId,
            UUID accountCodeId,
            String expenseCategory,
            String description,
            BigDecimal amount
    ) {
        requireBudgetPrivilege();

        BudgetHeader header = budgetHeaderRepository.findById(headerId)
                .orElseThrow(() -> new IllegalArgumentException("Budget header not found"));

        if (!"DRAFT".equals(header.getStatus())) {
            throw new IllegalStateException("Budget lines can only be added while the budget is in draft status.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Budget line amount must be greater than zero.");
        }

        BudgetLine line = new BudgetLine();
        line.setBudgetHeader(header);
        line.setExpenseCategory(expenseCategory);
        line.setDescription(description);
        line.setOriginalAmount(amount);
        line.setAdjustedAmount(BigDecimal.ZERO);
        line.setReservedAmount(BigDecimal.ZERO);
        line.setSpentAmount(BigDecimal.ZERO);
        line.setActive(true);

        budgetLineRepository.save(line);

        auditService.log(
                "BUDGET_LINE",
                line.getId(),
                "ADD_BUDGET_LINE",
                null,
                amount.toPlainString(),
                description
        );

        return line;
    }

    @Transactional
    public BudgetHeader submitHeader(UUID headerId) {
        requireBudgetPrivilege();

        BudgetHeader header = getHeader(headerId);

        if (!"DRAFT".equals(header.getStatus())) {
            throw new IllegalStateException("Only draft budgets can be submitted.");
        }

        BigDecimal total = budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(header.getId())
                .stream()
                .map(line -> nullSafe(line.getOriginalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.signum() <= 0) {
            throw new IllegalStateException("Cannot submit a budget with zero or negative total amount.");
        }

        header.setTotalAmount(total);
        header.setStatus("SUBMITTED");
        header.setSubmittedBy(currentUserService.username());
        header.setSubmittedAt(Instant.now());

        auditService.log(
                "BUDGET_HEADER",
                header.getId(),
                "SUBMIT_BUDGET",
                null,
                header.getStatus(),
                "Budget submitted"
        );

        return budgetHeaderRepository.save(header);
    }

    @Transactional
    public BudgetHeader approveHeader(UUID headerId) {
        requireBudgetPrivilege();

        BudgetHeader header = getHeader(headerId);

        if (!"SUBMITTED".equals(header.getStatus())) {
            throw new IllegalStateException("Only submitted budgets can be approved.");
        }

        header.setStatus("APPROVED");
        header.setApprovedBy(currentUserService.username());
        header.setApprovedAt(Instant.now());

        auditService.log(
                "BUDGET_HEADER",
                header.getId(),
                "APPROVE_BUDGET",
                null,
                header.getStatus(),
                "Budget approved"
        );

        return budgetHeaderRepository.save(header);
    }

    @Transactional
    public BudgetHeader lockHeader(UUID headerId) {
        requireBudgetPrivilege();

        BudgetHeader header = getHeader(headerId);

        if (!"APPROVED".equals(header.getStatus())) {
            throw new IllegalStateException("Only approved budgets can be locked.");
        }

        header.setStatus("LOCKED");
        header.setLockedBy(currentUserService.username());
        header.setLockedAt(Instant.now());

        auditService.log(
                "BUDGET_HEADER",
                header.getId(),
                "LOCK_BUDGET",
                null,
                header.getStatus(),
                "Budget locked"
        );

        return budgetHeaderRepository.save(header);
    }

    @Transactional
    public BudgetAdjustment adjustLine(UUID lineId, BigDecimal amount, String reason) {
        requireBudgetPrivilege();

        BudgetLine line = budgetLineRepository.findById(lineId)
                .orElseThrow(() -> new IllegalArgumentException("Budget line not found"));

        if (!"APPROVED".equals(line.getBudgetHeader().getStatus())) {
            throw new IllegalStateException("Budget adjustments are only allowed for approved budgets.");
        }

        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Adjustment amount cannot be zero.");
        }

        if (amount.signum() < 0) {
            BigDecimal available = availableAmount(line);

            if (available.compareTo(amount.abs()) < 0) {
                throw new IllegalStateException("Insufficient available budget for this reduction.");
            }
        }

        line.setAdjustedAmount(nullSafe(line.getAdjustedAmount()).add(amount));
        budgetLineRepository.save(line);

        BudgetAdjustment adjustment = new BudgetAdjustment();
        adjustment.setBudgetLine(line);
        adjustment.setAmount(amount);
        adjustment.setReason(reason);
        adjustment.setStatus("APPROVED");
        adjustment.setApprovedBy(currentUserService.username());
        adjustment.setApprovedAt(Instant.now());

        budgetAdjustmentRepository.save(adjustment);

        auditService.log(
                "BUDGET_LINE",
                line.getId(),
                "ADJUST_BUDGET",
                null,
                amount.toPlainString(),
                reason
        );

        return adjustment;
    }

    @Transactional
    public void reserveForPurchaseOrder(PurchaseOrder order) {
        if (order.getBudgetLine() == null) {
            return;
        }

        BudgetLine line = budgetLineRepository.findById(order.getBudgetLine().getId())
                .orElseThrow(() -> new IllegalArgumentException("Budget line not found"));

        if (!"APPROVED".equals(line.getBudgetHeader().getStatus())) {
            throw new IllegalStateException("Purchase orders can only reserve approved budgets.");
        }

        BigDecimal amount = nullSafe(order.getTotalAmount());

        if (amount.signum() == 0) {
            return;
        }

        BigDecimal available = availableAmount(line);

        if (available.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient available budget. Available: " + available + ", Required: " + amount
            );
        }

        line.setReservedAmount(nullSafe(line.getReservedAmount()).add(amount));
        budgetLineRepository.save(line);

        auditService.log(
                "PURCHASE_ORDER",
                order.getId(),
                "BUDGET_RESERVATION",
                null,
                amount.toPlainString(),
                "Budget reserved for purchase order " + order.getPoNumber()
        );
    }

    @Transactional
    public void registerInvoiceConsumption(SupplierInvoice invoice) {
        if (invoice.getBudgetLine() == null) {
            return;
        }

        BudgetLine line = budgetLineRepository.findById(invoice.getBudgetLine().getId())
                .orElseThrow(() -> new IllegalArgumentException("Budget line not found"));

        if (!"APPROVED".equals(line.getBudgetHeader().getStatus())) {
            throw new IllegalStateException("Supplier invoices can only consume approved budgets.");
        }

        BigDecimal amount = nullSafe(invoice.getTotalAmount());

        if (amount.signum() == 0) {
            return;
        }

        BigDecimal reserved = nullSafe(line.getReservedAmount());
        BigDecimal spent = nullSafe(line.getSpentAmount());
        BigDecimal adjusted = nullSafe(line.getAdjustedAmount());
        BigDecimal original = nullSafe(line.getOriginalAmount());

        BigDecimal reservedReduction = BigDecimal.ZERO;

        if (invoice.getPurchaseOrder() != null) {
            PurchaseOrder order = purchaseOrderRepository.findById(invoice.getPurchaseOrder().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));

            reservedReduction = amount.min(reserved);

            line.setReservedAmount(reserved.subtract(reservedReduction));

            order.setInvoicedAmount(nullSafe(order.getInvoicedAmount()).add(amount));
            purchaseOrderRepository.save(order);
        }

        BigDecimal projectedReserved = reserved.subtract(reservedReduction);
        BigDecimal projectedSpent = spent.add(amount);
        BigDecimal projectedAvailable = original.add(adjusted).subtract(projectedReserved).subtract(projectedSpent);

        if (projectedAvailable.signum() < 0) {
            throw new IllegalStateException(
                    "Supplier invoice exceeds available budget. Available before posting: " + availableAmount(line)
            );
        }

        line.setSpentAmount(projectedSpent);
        budgetLineRepository.save(line);

        auditService.log(
                "SUPPLIER_INVOICE",
                invoice.getId(),
                "BUDGET_CONSUMPTION",
                null,
                amount.toPlainString(),
                "Budget consumed by supplier invoice " + invoice.getInvoiceNumber()
        );
    }

    @Transactional
    public void registerDirectExpense(
            UUID budgetLineId,
            BigDecimal amount,
            String referenceType,
            UUID referenceId
    ) {
        requireBudgetPrivilege();

        BudgetLine line = budgetLineRepository.findById(budgetLineId)
                .orElseThrow(() -> new IllegalArgumentException("Budget line not found"));

        if (!"APPROVED".equals(line.getBudgetHeader().getStatus())) {
            throw new IllegalStateException("Direct expenses can only consume approved budgets.");
        }

        if (amount == null || amount.signum() == 0) {
            return;
        }

        BigDecimal available = availableAmount(line);

        if (available.compareTo(amount) < 0) {
            throw new IllegalStateException(
                    "Insufficient available budget. Available: " + available + ", Required: " + amount
            );
        }

        line.setSpentAmount(nullSafe(line.getSpentAmount()).add(amount));
        budgetLineRepository.save(line);

        auditService.log(
                "BUDGET_LINE",
                line.getId(),
                "DIRECT_EXPENSE",
                null,
                amount.toPlainString(),
                referenceType + " consumption"
        );
    }

    public BigDecimal availableAmount(BudgetLine line) {
        BigDecimal original = nullSafe(line.getOriginalAmount());
        BigDecimal adjusted = nullSafe(line.getAdjustedAmount());
        BigDecimal reserved = nullSafe(line.getReservedAmount());
        BigDecimal spent = nullSafe(line.getSpentAmount());

        return original.add(adjusted).subtract(reserved).subtract(spent);
    }

    private BudgetHeader getHeader(UUID headerId) {
        return budgetHeaderRepository.findById(headerId)
                .orElseThrow(() -> new IllegalArgumentException("Budget header not found"));
    }

    private void requireBudgetPrivilege() {
        if (!currentUserService.hasPrivilege("BUDGET_MANAGE")) {
            throw new AccessDeniedException("Current user does not have budget management privilege.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}