// src/main/java/com/institution/finance/service/CentralAccountingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.DepartmentSubmission;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import cm.ndicsonlabs.bossapp.domain.SubmissionTransaction;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentSubmissionRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SubmissionTransactionRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CentralAccountingService {

    private final CurrentUserService currentUserService;
    private final DepartmentRepository departmentRepository;
    private final AccountingPeriodRepository periodRepository;
    private final DepartmentSubmissionRepository submissionRepository;
    private final SubmissionTransactionRepository submissionTransactionRepository;
    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AuditService auditService;

    public CentralAccountingService(
            CurrentUserService currentUserService, DepartmentRepository departmentRepository,
            AccountingPeriodRepository periodRepository,
            DepartmentSubmissionRepository submissionRepository,
            SubmissionTransactionRepository submissionTransactionRepository,
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository,
            AuditService auditService
    ) {
        this.currentUserService = currentUserService;
        this.departmentRepository = departmentRepository;
        this.periodRepository = periodRepository;
        this.submissionRepository = submissionRepository;
        this.submissionTransactionRepository = submissionTransactionRepository;
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DepartmentSubmission prepareDraft(UUID departmentId, UUID periodId) {
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        AccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));

        if ("LOCKED".equals(period.getStatus())) {
            throw new IllegalStateException("Accounting period is locked.");
        }

        boolean activeSubmissionExists = submissionRepository.existsByPeriodAndDepartmentAndStatusIn(
                period,
                department,
                List.of(
                        "DRAFT",
                        "DEPARTMENT_APPROVED",
                        "SUBMITTED",
                        "UNDER_CENTRAL_REVIEW",
                        "ACCEPTED"
                )
        );

        if (activeSubmissionExists) {
            throw new IllegalStateException(
                    "An active or accepted submission already exists for this department and period."
            );
        }

        List<String> reusableStatuses = List.of("NOT_SUBMITTED", "RETURNED");

        List<SupplierInvoice> supplierInvoices = supplierInvoiceRepository
                .findByDepartmentAndInvoiceDateBetweenAndAccountingStatusIn(
                        department,
                        period.getStartDate(),
                        period.getEndDate(),
                        reusableStatuses
                );

        List<StudentCharge> studentCharges = studentChargeRepository
                .findByDepartmentAndChargeDateBetweenAndAccountingStatusIn(
                        department,
                        period.getStartDate(),
                        period.getEndDate(),
                        reusableStatuses
                );

        List<PatientCharge> patientCharges = patientChargeRepository
                .findByDepartmentAndChargeDateBetweenAndAccountingStatusIn(
                        department,
                        period.getStartDate(),
                        period.getEndDate(),
                        reusableStatuses
                );

        List<Payment> payments = paymentRepository
                .findByDepartmentAndPaymentDateBetweenAndAccountingStatusIn(
                        department,
                        period.getStartDate(),
                        period.getEndDate(),
                        reusableStatuses
                );

        if (supplierInvoices.isEmpty() && studentCharges.isEmpty() && patientCharges.isEmpty() && payments.isEmpty()) {
            throw new IllegalStateException("No unsubmitted transactions found for this department and period.");
        }

        DepartmentSubmission submission = new DepartmentSubmission();
        submission.setDepartment(department);
        submission.setPeriod(period);
        submission.setStatus("DRAFT");
        submission.setCreatedBy(currentUsername());

        BigDecimal openingAp = calculateOpeningAp(department, period.getStartDate());
        BigDecimal newAp = supplierInvoices.stream()
                .map(invoice -> nullSafe(invoice.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Payment> outPayments = payments.stream()
                .filter(payment -> "OUT".equals(payment.getDirection()))
                .toList();

        BigDecimal apPayments = outPayments.stream()
                .map(payment -> nullSafe(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal openingAr = calculateOpeningAr(department, period.getStartDate());

        BigDecimal newAr = studentCharges.stream()
                .map(charge -> nullSafe(charge.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .add(
                        patientCharges.stream()
                                .map(charge -> nullSafe(charge.getAmount()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                );

        List<Payment> inPayments = payments.stream()
                .filter(payment -> "IN".equals(payment.getDirection()))
                .toList();

        BigDecimal arCollections = inPayments.stream()
                .map(payment -> nullSafe(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        submission.setOpeningApBalance(openingAp);
        submission.setNewApAmount(newAp);
        submission.setApPayments(apPayments);
        submission.setClosingApBalance(openingAp.add(newAp).subtract(apPayments));

        submission.setOpeningArBalance(openingAr);
        submission.setNewArAmount(newAr);
        submission.setArCollections(arCollections);
        submission.setClosingArBalance(openingAr.add(newAr).subtract(arCollections));

        submission.setTransactionCount(
                supplierInvoices.size() + studentCharges.size() + patientCharges.size() + payments.size()
        );

        submissionRepository.save(submission);

        List<SubmissionTransaction> lines = new ArrayList<>();

        for (SupplierInvoice invoice : supplierInvoices) {
            invoice.setAccountingStatus("DRAFT");

            SubmissionTransaction line = new SubmissionTransaction();
            line.setSubmission(submission);
            line.setTargetType("SUPPLIER_INVOICE");
            line.setTargetId(invoice.getId());
            line.setDirection("AP");
            line.setTransactionDate(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : period.getStartDate());
            line.setAmount(nullSafe(invoice.getTotalAmount()));
            lines.add(line);
        }

        for (StudentCharge charge : studentCharges) {
            charge.setAccountingStatus("DRAFT");

            SubmissionTransaction line = new SubmissionTransaction();
            line.setSubmission(submission);
            line.setTargetType("STUDENT_CHARGE");
            line.setTargetId(charge.getId());
            line.setDirection("AR");
            line.setTransactionDate(charge.getChargeDate() != null ? charge.getChargeDate() : period.getStartDate());
            line.setAmount(nullSafe(charge.getAmount()));
            lines.add(line);
        }

        for (PatientCharge charge : patientCharges) {
            charge.setAccountingStatus("DRAFT");

            SubmissionTransaction line = new SubmissionTransaction();
            line.setSubmission(submission);
            line.setTargetType("PATIENT_CHARGE");
            line.setTargetId(charge.getId());
            line.setDirection("AR");
            line.setTransactionDate(charge.getChargeDate() != null ? charge.getChargeDate() : period.getStartDate());
            line.setAmount(nullSafe(charge.getAmount()));
            lines.add(line);
        }

        for (Payment payment : payments) {
            payment.setAccountingStatus("DRAFT");

            SubmissionTransaction line = new SubmissionTransaction();
            line.setSubmission(submission);
            line.setTargetType("PAYMENT");
            line.setTargetId(payment.getId());
            line.setDirection(payment.getDirection());
            line.setTransactionDate(payment.getPaymentDate() != null ? payment.getPaymentDate() : period.getStartDate());
            line.setAmount(nullSafe(payment.getAmount()));
            lines.add(line);
        }

        supplierInvoiceRepository.saveAll(supplierInvoices);
        studentChargeRepository.saveAll(studentCharges);
        patientChargeRepository.saveAll(patientCharges);
        paymentRepository.saveAll(payments);
        submissionTransactionRepository.saveAll(lines);

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "PREPARE_DRAFT",
                null,
                submission.getStatus(),
                "Department submission draft prepared"
        );

        return submission;
    }

    @Transactional
    public DepartmentSubmission approveByDepartment(UUID submissionId) {
        DepartmentSubmission submission = getSubmission(submissionId);

        if (!"DRAFT".equals(submission.getStatus())) {
            throw new IllegalStateException("Only draft submissions can be approved by the department.");
        }

        submission.setStatus("DEPARTMENT_APPROVED");
        submission.setDepartmentApprovedBy(currentUsername());
        submission.setDepartmentApprovedAt(Instant.now());

        updateTransactionStatuses(submission, "DEPARTMENT_APPROVED");

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "DEPARTMENT_APPROVE",
                null,
                submission.getStatus(),
                "Department approved submission"
        );

        return submissionRepository.save(submission);
    }

    @Transactional
    public DepartmentSubmission submitToCentral(UUID submissionId) {
        DepartmentSubmission submission = getSubmission(submissionId);

        if (!"DEPARTMENT_APPROVED".equals(submission.getStatus())) {
            throw new IllegalStateException("Submission must be approved by the department before submission.");
        }

        submission.setStatus("SUBMITTED");
        submission.setSubmittedBy(currentUsername());
        submission.setSubmittedAt(Instant.now());

        updateTransactionStatuses(submission, "SUBMITTED");

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "SUBMIT_TO_CENTRAL",
                null,
                submission.getStatus(),
                "Submission sent to central accounting"
        );

        return submissionRepository.save(submission);
    }

    @Transactional
    public DepartmentSubmission startCentralReview(UUID submissionId) {
        DepartmentSubmission submission = getSubmission(submissionId);

        if (!"SUBMITTED".equals(submission.getStatus())) {
            throw new IllegalStateException("Only submitted submissions can be moved under central review.");
        }

        submission.setStatus("UNDER_CENTRAL_REVIEW");
        submission.setCentralReviewedBy(currentUsername());
        submission.setCentralReviewedAt(Instant.now());

        updateTransactionStatuses(submission, "UNDER_CENTRAL_REVIEW");

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "START_CENTRAL_REVIEW",
                null,
                submission.getStatus(),
                "Central accounting started review"
        );

        return submissionRepository.save(submission);
    }

    @Transactional
    public DepartmentSubmission accept(UUID submissionId) {
        DepartmentSubmission submission = getSubmission(submissionId);

        if (!"SUBMITTED".equals(submission.getStatus()) && !"UNDER_CENTRAL_REVIEW".equals(submission.getStatus())) {
            throw new IllegalStateException("Only submitted or under-review submissions can be accepted.");
        }

        submission.setStatus("ACCEPTED");
        submission.setCentralReviewedBy(currentUsername());
        submission.setCentralReviewedAt(Instant.now());

        updateTransactionStatuses(submission, "ACCEPTED");

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "CENTRAL_ACCEPT",
                null,
                submission.getStatus(),
                "Central accounting accepted submission"
        );

        return submissionRepository.save(submission);
    }

    @Transactional
    public DepartmentSubmission reject(UUID submissionId, String comments) {
        DepartmentSubmission submission = getSubmission(submissionId);

        if (!"SUBMITTED".equals(submission.getStatus()) && !"UNDER_CENTRAL_REVIEW".equals(submission.getStatus())) {
            throw new IllegalStateException("Only submitted or under-review submissions can be rejected.");
        }

        submission.setStatus("REJECTED");
        submission.setReviewComments(comments);
        submission.setCentralReviewedBy(currentUsername());
        submission.setCentralReviewedAt(Instant.now());

        updateTransactionStatuses(submission, "RETURNED");

        auditService.log(
                "DEPARTMENT_SUBMISSION",
                submission.getId(),
                "CENTRAL_REJECT",
                null,
                submission.getStatus(),
                comments
        );

        return submissionRepository.save(submission);
    }

//    @Transactional
//    public AccountingPeriod lockPeriod(UUID periodId) {
//        AccountingPeriod period = periodRepository.findById(periodId)
//                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));
//
//        boolean activeSubmissionExists = submissionRepository.existsByPeriodAndStatusIn(
//                period,
//                List.of(
//                        "DRAFT",
//                        "DEPARTMENT_APPROVED",
//                        "SUBMITTED",
//                        "UNDER_CENTRAL_REVIEW"
//                )
//        );
//
//        if (activeSubmissionExists) {
//            throw new IllegalStateException(
//                    "Period cannot be locked while draft, approved, submitted, or under-review submissions exist."
//            );
//        }
//
//        period.setStatus("LOCKED");
//        period.setClosedBy(currentUsername());
//        period.setLockedDate(LocalDate.now());
//
//        auditService.log(
//                "ACCOUNTING_PERIOD",
//                period.getId(),
//                "LOCK_PERIOD",
//                null,
//                period.getStatus(),
//                "Accounting period locked"
//        );
//
//        return periodRepository.save(period);
//    }
// src/main/java/com/institution/finance/service/CentralAccountingService.java

    @Transactional
    public AccountingPeriod openPeriod(UUID periodId, String reason) {
        requirePrivilege("ACCOUNTING_PERIOD_OPEN");

        AccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));

        if (!"LOCKED".equals(period.getStatus())) {
            throw new IllegalStateException("Only locked periods can be opened.");
        }

        period.setStatus("OPEN");
        period.setReopenedBy(currentUserService.username());
        period.setReopenedAt(Instant.now());
        period.setReopenReason(reason);
        period.setLockedDate(null);
        period.setClosedBy(null);

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "OPEN_PERIOD",
                null,
                period.getStatus(),
                reason
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod lockPeriod(UUID periodId) {
        requirePrivilege("ACCOUNTING_PERIOD_LOCK");

        AccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));

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
            throw new IllegalStateException(
                    "Period cannot be locked while draft, approved, submitted, or under-review submissions exist."
            );
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
                "Accounting period locked"
        );

        return periodRepository.save(period);
    }

    private void requirePrivilege(String privilegeCode) {
        if (!currentUserService.hasPrivilege(privilegeCode)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Current user does not have privilege: " + privilegeCode
            );
        }
    }
    private DepartmentSubmission getSubmission(UUID submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }

    private void updateTransactionStatuses(DepartmentSubmission submission, String status) {
        List<SubmissionTransaction> lines = submissionTransactionRepository.findBySubmissionId(submission.getId());

        for (SubmissionTransaction line : lines) {
            updateTargetStatus(line.getTargetType(), line.getTargetId(), status);
        }
    }

    private void updateTargetStatus(String targetType, UUID targetId, String status) {
        switch (targetType) {
            case "STUDENT_CHARGE" -> studentChargeRepository.findById(targetId).ifPresent(charge -> {
                charge.setAccountingStatus(status);
                studentChargeRepository.save(charge);
            });

            case "PATIENT_CHARGE" -> patientChargeRepository.findById(targetId).ifPresent(charge -> {
                charge.setAccountingStatus(status);
                patientChargeRepository.save(charge);
            });

            case "SUPPLIER_INVOICE" -> supplierInvoiceRepository.findById(targetId).ifPresent(invoice -> {
                invoice.setAccountingStatus(status);
                supplierInvoiceRepository.save(invoice);
            });

            case "PAYMENT" -> paymentRepository.findById(targetId).ifPresent(payment -> {
                payment.setAccountingStatus(status);
                paymentRepository.save(payment);
            });

            default -> throw new IllegalStateException("Unknown submission target type: " + targetType);
        }
    }

    private BigDecimal calculateOpeningAp(Department department, LocalDate periodStart) {
        return supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> department.getId().equals(invoice.getDepartment().getId()))
                .filter(invoice -> invoice.getInvoiceDate() != null)
                .filter(invoice -> invoice.getInvoiceDate().isBefore(periodStart))
                .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateOpeningAr(Department department, LocalDate periodStart) {
        BigDecimal studentOpening = studentChargeRepository.findAll()
                .stream()
                .filter(charge -> department.getId().equals(charge.getDepartment().getId()))
                .filter(charge -> charge.getChargeDate() != null)
                .filter(charge -> charge.getChargeDate().isBefore(periodStart))
                .map(charge -> nullSafe(charge.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal patientOpening = patientChargeRepository.findAll()
                .stream()
                .filter(charge -> department.getId().equals(charge.getDepartment().getId()))
                .filter(charge -> charge.getChargeDate() != null)
                .filter(charge -> charge.getChargeDate().isBefore(periodStart))
                .map(charge -> nullSafe(charge.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return studentOpening.add(patientOpening);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "system";
        }

        return authentication.getName();
    }
}