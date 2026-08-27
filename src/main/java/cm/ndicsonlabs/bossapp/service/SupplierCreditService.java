package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.PharmacyDailyReconciliation;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierBatch;
import cm.ndicsonlabs.bossapp.domain.SupplierCreditAlert;
import cm.ndicsonlabs.bossapp.domain.SupplierCreditControl;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.dto.AgingLine;
import cm.ndicsonlabs.bossapp.dto.SupplierCreditSummary;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.PharmacyDailyReconciliationRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierBatchRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierCreditAlertRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierCreditControlRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierCreditService {

    private final SupplierRepository supplierRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final SupplierCreditControlRepository creditControlRepository;
    private final SupplierCreditAlertRepository alertRepository;
    private final SupplierBatchRepository batchRepository;
    private final PaymentRepository paymentRepository;
    private final PharmacyDailyReconciliationRepository pharmacyReconciliationRepository;
    private final CurrentUserService currentUserService;

    public SupplierCreditService(
            SupplierRepository supplierRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            SupplierCreditControlRepository creditControlRepository,
            SupplierCreditAlertRepository alertRepository,
            SupplierBatchRepository batchRepository,
            PaymentRepository paymentRepository,
            PharmacyDailyReconciliationRepository pharmacyReconciliationRepository,
            CurrentUserService currentUserService
    ) {
        this.supplierRepository = supplierRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.creditControlRepository = creditControlRepository;
        this.alertRepository = alertRepository;
        this.batchRepository = batchRepository;
        this.paymentRepository = paymentRepository;
        this.pharmacyReconciliationRepository = pharmacyReconciliationRepository;
        this.currentUserService = currentUserService;
    }

    public List<SupplierCreditSummary> creditSummaries() {
        List<SupplierCreditSummary> summaries = new ArrayList<>();

        for (Supplier supplier : supplierRepository.findAll()) {
            BigDecimal outstanding = outstandingBalance(supplier.getId());

            SupplierCreditControl control = creditControlRepository.findBySupplierId(supplier.getId())
                    .orElse(null);

            BigDecimal limit = control != null && control.getCreditLimit() != null
                    ? control.getCreditLimit()
                    : supplier.getCreditLimit();

            BigDecimal available = null;
            BigDecimal utilization = null;

            if (limit != null) {
                available = limit.subtract(outstanding);

                if (limit.signum() > 0) {
                    utilization = outstanding
                            .divide(limit, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            summaries.add(new SupplierCreditSummary(
                    supplier.getId(),
                    supplier.getCode(),
                    supplier.getName(),
                    supplier.getCategory(),
                    supplier.getSupplierSubcategory(),
                    limit,
                    outstanding,
                    available,
                    utilization,
                    supplier.isCreditHold()
            ));
        }

        return summaries.stream()
                .sorted(Comparator.comparing(SupplierCreditSummary::getSupplierCode))
                .toList();
    }

    @Transactional
    public SupplierCreditControl saveCreditControl(
            UUID supplierId,
            BigDecimal creditLimit,
            Integer creditTermsDays,
            Integer alertThresholdDays,
            boolean holdOnLimitExceeded
    ) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        SupplierCreditControl control = creditControlRepository.findBySupplierId(supplierId)
                .orElseGet(() -> {
                    SupplierCreditControl newControl = new SupplierCreditControl();
                    newControl.setSupplier(supplier);
                    return newControl;
                });

        control.setCreditLimit(creditLimit);
        control.setCreditTermsDays(creditTermsDays);
        control.setAlertThresholdDays(alertThresholdDays != null ? alertThresholdDays : 7);
        control.setHoldOnLimitExceeded(holdOnLimitExceeded);
        control.setActive(true);

        supplier.setCreditLimit(creditLimit);

        if (creditTermsDays != null) {
            supplier.setPaymentTermsDays(creditTermsDays);
        }

        supplierRepository.save(supplier);

        return creditControlRepository.save(control);
    }

    public void validateSupplierCredit(UUID supplierId, BigDecimal additionalAmount) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        if (supplier.isCreditHold()) {
            throw new IllegalStateException("Supplier is on credit hold.");
        }

        SupplierCreditControl control = creditControlRepository.findBySupplierId(supplierId)
                .orElse(null);

        BigDecimal limit = control != null && control.getCreditLimit() != null
                ? control.getCreditLimit()
                : supplier.getCreditLimit();

        if (limit == null) {
            return;
        }

        boolean holdEnabled = control != null && control.isHoldOnLimitExceeded();

        if (!holdEnabled) {
            return;
        }

        BigDecimal outstanding = outstandingBalance(supplierId);
        BigDecimal projected = outstanding.add(nullSafe(additionalAmount));

        if (projected.compareTo(limit) > 0) {
            throw new IllegalStateException(
                    "Supplier credit limit exceeded. Outstanding: " + outstanding +
                            ", New amount: " + nullSafe(additionalAmount) +
                            ", Limit: " + limit
            );
        }
    }

    @Transactional
    public int generateAlerts() {
        List<SupplierCreditAlert> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (SupplierInvoice invoice : supplierInvoiceRepository.findAll()) {
            BigDecimal remaining = nullSafe(invoice.getRemainingAmount());

            if (remaining.signum() <= 0) {
                continue;
            }

            Supplier supplier = invoice.getSupplier();
            int threshold = alertThresholdFor(supplier.getId());

            if (invoice.getDueDate() != null && invoice.getDueDate().isBefore(today)) {
                if (!alertRepository.existsBySupplierIdAndAlertTypeAndSourceId(
                        supplier.getId(),
                        "OVERDUE_INVOICE",
                        invoice.getId()
                )) {
                    alerts.add(createAlert(
                            supplier,
                            "SUPPLIER_INVOICE",
                            invoice.getId(),
                            "OVERDUE_INVOICE",
                            "ERROR",
                            "Supplier invoice " + invoice.getInvoiceNumber() + " is overdue.",
                            invoice.getDueDate(),
                            remaining
                    ));
                }
            }

            if (invoice.getDueDate() != null &&
                    !invoice.getDueDate().isBefore(today) &&
                    !invoice.getDueDate().isAfter(today.plusDays(threshold))) {
                if (!alertRepository.existsBySupplierIdAndAlertTypeAndSourceId(
                        supplier.getId(),
                        "DUE_SOON",
                        invoice.getId()
                )) {
                    alerts.add(createAlert(
                            supplier,
                            "SUPPLIER_INVOICE",
                            invoice.getId(),
                            "DUE_SOON",
                            "WARNING",
                            "Supplier invoice " + invoice.getInvoiceNumber() + " is due soon.",
                            invoice.getDueDate(),
                            remaining
                    ));
                }
            }
        }

        for (SupplierCreditSummary summary : creditSummaries()) {
            if (summary.getCreditLimit() != null &&
                    summary.getOutstanding().compareTo(summary.getCreditLimit()) > 0) {
                if (!alertRepository.existsBySupplierIdAndAlertTypeAndSourceId(
                        summary.getSupplierId(),
                        "CREDIT_LIMIT_EXCEEDED",
                        summary.getSupplierId()
                )) {
                    Supplier supplier = supplierRepository.findById(summary.getSupplierId())
                            .orElseThrow();

                    alerts.add(createAlert(
                            supplier,
                            "SUPPLIER",
                            summary.getSupplierId(),
                            "CREDIT_LIMIT_EXCEEDED",
                            "ERROR",
                            "Supplier outstanding balance exceeds credit limit.",
                            null,
                            summary.getOutstanding().subtract(summary.getCreditLimit())
                    ));
                }
            }
        }

        for (SupplierBatch batch : batchRepository.findByExpiryDateLessThanEqualAndStatus(
                today.plusDays(30),
                "ACTIVE"
        )) {
            if (!alertRepository.existsBySupplierIdAndAlertTypeAndSourceId(
                    batch.getSupplier().getId(),
                    "BATCH_EXPIRING_SOON",
                    batch.getId()
            )) {
                alerts.add(createAlert(
                        batch.getSupplier(),
                        "SUPPLIER_BATCH",
                        batch.getId(),
                        "BATCH_EXPIRING_SOON",
                        "WARNING",
                        "Batch " + batch.getBatchNumber() + " expires soon.",
                        batch.getExpiryDate(),
                        nullSafe(batch.getAmount())
                ));
            }
        }

        alertRepository.saveAll(alerts);

        return alerts.size();
    }

    @Transactional
    public SupplierCreditAlert acknowledgeAlert(UUID alertId) {
        SupplierCreditAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found"));

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(currentUserService.username());
        alert.setAcknowledgedAt(Instant.now());

        return alertRepository.save(alert);
    }

    public List<AgingLine> supplierCreditAging(String categoryFilter) {
        List<AgingLine> lines = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (SupplierInvoice invoice : supplierInvoiceRepository.findAll()) {
            BigDecimal remaining = nullSafe(invoice.getRemainingAmount());

            if (remaining.signum() <= 0) {
                continue;
            }

            Supplier supplier = invoice.getSupplier();

            if (categoryFilter != null && !categoryFilter.isBlank()) {
                boolean matchesCategory = categoryFilter.equalsIgnoreCase(supplier.getCategory());
                boolean matchesSubcategory = categoryFilter.equalsIgnoreCase(supplier.getSupplierSubcategory());

                if (!matchesCategory && !matchesSubcategory) {
                    continue;
                }
            }

            LocalDate dueDate = invoice.getDueDate() != null ? invoice.getDueDate() : invoice.getInvoiceDate();

            long overdueDays = 0;
            String bucket = "Current";

            if (dueDate != null && dueDate.isBefore(today)) {
                overdueDays = ChronoUnit.DAYS.between(dueDate, today);
                bucket = agingBucket(overdueDays);
            }

            lines.add(new AgingLine(
                    supplier.getName(),
                    invoice.getInvoiceNumber(),
                    dueDate != null ? dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                    nullSafe(invoice.getTotalAmount()),
                    nullSafe(invoice.getPaidAmount()),
                    remaining,
                    overdueDays,
                    bucket
            ));
        }

        return lines.stream()
                .sorted(Comparator.comparing(AgingLine::getDueDate))
                .toList();
    }

    @Transactional
    public PharmacyDailyReconciliation openPharmacyReconciliation(UUID departmentId, LocalDate date) {
        if (pharmacyReconciliationRepository.findByDepartmentIdAndReconciliationDate(departmentId, date).isPresent()) {
            throw new IllegalStateException("A pharmacy reconciliation already exists for this department and date.");
        }

        Department department = new Department();
        department.setId(departmentId);

        BigDecimal opening = calculateOpeningSupplierCredit(departmentId, date);
        BigDecimal newInvoices = calculateNewSupplierInvoices(departmentId, date);
        BigDecimal payments = calculateSupplierPayments(departmentId, date);
        BigDecimal expectedClosing = opening.add(newInvoices).subtract(payments);

        PharmacyDailyReconciliation reconciliation = new PharmacyDailyReconciliation();
        reconciliation.setDepartment(department);
        reconciliation.setReconciliationDate(date);
        reconciliation.setOpeningSupplierCredit(opening);
        reconciliation.setNewSupplierInvoices(newInvoices);
        reconciliation.setSupplierPayments(payments);
        reconciliation.setExpectedClosingCredit(expectedClosing);
        reconciliation.setStatus("OPEN");

        return pharmacyReconciliationRepository.save(reconciliation);
    }

    @Transactional
    public PharmacyDailyReconciliation closePharmacyReconciliation(
            UUID reconciliationId,
            BigDecimal actualClosingCredit,
            String explanation
    ) {
        PharmacyDailyReconciliation reconciliation = pharmacyReconciliationRepository.findById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy reconciliation not found"));

        if (!"OPEN".equals(reconciliation.getStatus())) {
            throw new IllegalStateException("Only open pharmacy reconciliations can be closed.");
        }

        BigDecimal actual = nullSafe(actualClosingCredit);

        reconciliation.setActualClosingCredit(actual);
        reconciliation.setVariance(actual.subtract(reconciliation.getExpectedClosingCredit()));
        reconciliation.setExplanation(explanation);
        reconciliation.setStatus("CLOSED");

        return pharmacyReconciliationRepository.save(reconciliation);
    }

    @Transactional
    public PharmacyDailyReconciliation approvePharmacyReconciliation(UUID reconciliationId) {
        PharmacyDailyReconciliation reconciliation = pharmacyReconciliationRepository.findById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Pharmacy reconciliation not found"));

        if (!"CLOSED".equals(reconciliation.getStatus())) {
            throw new IllegalStateException("Only closed pharmacy reconciliations can be approved.");
        }

        reconciliation.setStatus("APPROVED");
        reconciliation.setApprovedBy(currentUserService.username());
        reconciliation.setApprovedAt(Instant.now());

        return pharmacyReconciliationRepository.save(reconciliation);
    }

    private BigDecimal outstandingBalance(UUID supplierId) {
        return supplierInvoiceRepository.findBySupplierId(supplierId)
                .stream()
                .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int alertThresholdFor(UUID supplierId) {
        return creditControlRepository.findBySupplierId(supplierId)
                .map(SupplierCreditControl::getAlertThresholdDays)
                .orElse(7);
    }

    private SupplierCreditAlert createAlert(
            Supplier supplier,
            String sourceType,
            UUID sourceId,
            String alertType,
            String severity,
            String message,
            LocalDate dueDate,
            BigDecimal amount
    ) {
        SupplierCreditAlert alert = new SupplierCreditAlert();
        alert.setSupplier(supplier);
        alert.setSourceType(sourceType);
        alert.setSourceId(sourceId);
        alert.setAlertType(alertType);
        alert.setSeverity(severity);
        alert.setMessage(message);
        alert.setDueDate(dueDate);
        alert.setAmount(amount);
        alert.setAcknowledged(false);

        return alert;
    }

    private BigDecimal calculateOpeningSupplierCredit(UUID departmentId, LocalDate date) {
        return supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getDepartment() != null)
                .filter(invoice -> departmentId.equals(invoice.getDepartment().getId()))
                .filter(invoice -> invoice.getInvoiceDate() != null)
                .filter(invoice -> invoice.getInvoiceDate().isBefore(date))
                .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateNewSupplierInvoices(UUID departmentId, LocalDate date) {
        return supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getDepartment() != null)
                .filter(invoice -> departmentId.equals(invoice.getDepartment().getId()))
                .filter(invoice -> invoice.getInvoiceDate() != null)
                .filter(invoice -> invoice.getInvoiceDate().isEqual(date))
                .map(invoice -> nullSafe(invoice.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateSupplierPayments(UUID departmentId, LocalDate date) {
        return paymentRepository.findByDepartmentAndPaymentDate(
                        new Department() {{
                            setId(departmentId);
                        }},
                        date
                )
                .stream()
                .filter(payment -> "OUT".equals(payment.getDirection()))
                .map(payment -> nullSafe(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String agingBucket(long overdueDays) {
        if (overdueDays <= 30) {
            return "1-30";
        }

        if (overdueDays <= 60) {
            return "31-60";
        }

        if (overdueDays <= 90) {
            return "61-90";
        }

        if (overdueDays <= 120) {
            return "91-120";
        }

        return "Over 120";
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}