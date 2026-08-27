// src/main/java/com/institution/finance/service/EducationFinanceService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.FeeSchedule;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.PaymentAllocation;
import cm.ndicsonlabs.bossapp.domain.PaymentPlanInstallment;
import cm.ndicsonlabs.bossapp.domain.Student;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import cm.ndicsonlabs.bossapp.domain.StudentChargeAdjustment;
import cm.ndicsonlabs.bossapp.domain.StudentEnrollment;
import cm.ndicsonlabs.bossapp.domain.StudentPaymentPlan;
import cm.ndicsonlabs.bossapp.domain.StudentReceipt;
import cm.ndicsonlabs.bossapp.repository.FeeScheduleRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentAllocationRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentPlanInstallmentRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeAdjustmentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.StudentEnrollmentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentPaymentPlanRepository;
import cm.ndicsonlabs.bossapp.repository.StudentReceiptRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EducationFinanceService {

    private final StudentChargeRepository studentChargeRepository;
    private final StudentChargeAdjustmentRepository studentChargeAdjustmentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;
    private final StudentReceiptRepository studentReceiptRepository;
    private final FeeScheduleRepository feeScheduleRepository;
    private final StudentEnrollmentRepository studentEnrollmentRepository;
    private final StudentPaymentPlanRepository studentPaymentPlanRepository;
    private final PaymentPlanInstallmentRepository paymentPlanInstallmentRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;
    private final GlIntegrationService glIntegrationService;

    public EducationFinanceService(
            StudentChargeRepository studentChargeRepository,
            StudentChargeAdjustmentRepository studentChargeAdjustmentRepository,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository,
            StudentReceiptRepository studentReceiptRepository,
            FeeScheduleRepository feeScheduleRepository,
            StudentEnrollmentRepository studentEnrollmentRepository,
            StudentPaymentPlanRepository studentPaymentPlanRepository,
            PaymentPlanInstallmentRepository paymentPlanInstallmentRepository,
            CurrentUserService currentUserService,
            AuditService auditService, GlIntegrationService glIntegrationService
    ) {
        this.studentChargeRepository = studentChargeRepository;
        this.studentChargeAdjustmentRepository = studentChargeAdjustmentRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
        this.studentReceiptRepository = studentReceiptRepository;
        this.feeScheduleRepository = feeScheduleRepository;
        this.studentEnrollmentRepository = studentEnrollmentRepository;
        this.studentPaymentPlanRepository = studentPaymentPlanRepository;
        this.paymentPlanInstallmentRepository = paymentPlanInstallmentRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
        this.glIntegrationService = glIntegrationService;
    }

    @Transactional
    public StudentChargeAdjustment applyAdjustment(
            UUID studentChargeId,
            String adjustmentType,
            BigDecimal amount,
            String reason
    ) {
        requireEducationPrivilege();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Adjustment amount must be greater than zero.");
        }

        StudentCharge charge = studentChargeRepository.findById(studentChargeId)
                .orElseThrow(() -> new IllegalArgumentException("Student charge not found"));

        BigDecimal original = nullSafe(charge.getOriginalAmount());
        BigDecimal currentAdjustments = nullSafe(charge.getDiscountAmount())
                .add(nullSafe(charge.getScholarshipAmount()))
                .add(nullSafe(charge.getWaiverAmount()));

        BigDecimal newTotalAdjustments = currentAdjustments.add(amount);

        if (newTotalAdjustments.compareTo(original) > 0) {
            throw new IllegalArgumentException("Total adjustments cannot exceed original charge amount.");
        }

        switch (adjustmentType) {
            case "DISCOUNT" -> charge.setDiscountAmount(nullSafe(charge.getDiscountAmount()).add(amount));
            case "SCHOLARSHIP" -> charge.setScholarshipAmount(nullSafe(charge.getScholarshipAmount()).add(amount));
            case "WAIVER" -> charge.setWaiverAmount(nullSafe(charge.getWaiverAmount()).add(amount));
            default -> throw new IllegalArgumentException("Unsupported adjustment type: " + adjustmentType);
        }

        charge.setNetAmount(original.subtract(newTotalAdjustments));

        if (charge.getNetAmount().compareTo(nullSafe(charge.getPaidAmount())) < 0) {
            throw new IllegalArgumentException("Adjustment cannot reduce net amount below amount already paid.");
        }

        StudentChargeAdjustment adjustment = new StudentChargeAdjustment();
        adjustment.setStudentCharge(charge);
        adjustment.setAdjustmentType(adjustmentType);
        adjustment.setAmount(amount);
        adjustment.setReason(reason);
        adjustment.setApprovedBy(currentUserService.username());
        adjustment.setApprovedAt(Instant.now());
        adjustment.setStatus("APPROVED");

        studentChargeRepository.save(charge);
        studentChargeAdjustmentRepository.save(adjustment);

        auditService.log(
                "STUDENT_CHARGE",
                charge.getId(),
                "APPLY_" + adjustmentType,
                null,
                charge.getNetAmount().toPlainString(),
                reason
        );

        return adjustment;
    }

    @Transactional
    public StudentReceipt recordStudentPaymentWithReceipt(
            UUID studentChargeId,
            BigDecimal amount,
            String payer,
            String paymentMethod,
            LocalDate paymentDate
    ) {
        requireEducationPrivilege();

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        StudentCharge charge = studentChargeRepository.findById(studentChargeId)
                .orElseThrow(() -> new IllegalArgumentException("Student charge not found"));

        if (amount.compareTo(charge.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining student charge balance.");
        }

        Payment payment = new Payment();
        payment.setPaymentNumber("PMT-" + UUID.randomUUID());
        payment.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        payment.setAmount(amount);
        payment.setUnallocatedAmount(BigDecimal.ZERO);
        payment.setDirection("IN");
        payment.setPayerOrPayee(payer);
        payment.setMethod(paymentMethod != null ? paymentMethod : "CASH");
        payment.setStatus("POSTED");
        payment.setDepartment(charge.getDepartment());
        payment.setAccountingStatus("NOT_SUBMITTED");
        paymentRepository.save(payment);

        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setPayment(payment);
        allocation.setTargetType("STUDENT_CHARGE");
        allocation.setTargetId(charge.getId());
        allocation.setAmount(amount);
        paymentAllocationRepository.save(allocation);

        charge.setPaidAmount(nullSafe(charge.getPaidAmount()).add(amount));
        charge.setStatus(charge.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID");
        studentChargeRepository.save(charge);

        StudentReceipt receipt = new StudentReceipt();
        receipt.setReceiptNumber("RCT-" + UUID.randomUUID());
        receipt.setPayment(payment);
        receipt.setStudent(charge.getStudent());
        receipt.setDepartment(charge.getDepartment());
        receipt.setReceivedDate(payment.getPaymentDate());
        receipt.setAmount(amount);
        receipt.setPaymentMethod(payment.getMethod());
        receipt.setPayer(payer);
        receipt.setCashier(currentUserService.username());
        receipt.setStatus("POSTED");
        studentReceiptRepository.save(receipt);

        auditService.log(
                "STUDENT_CHARGE",
                charge.getId(),
                "STUDENT_PAYMENT_WITH_RECEIPT",
                null,
                amount.toPlainString(),
                "Receipt " + receipt.getReceiptNumber()
        );

        return receipt;
    }

    @Transactional
    public int generateChargesForFeeSchedule(UUID feeScheduleId) {
        requireEducationPrivilege();

        FeeSchedule schedule = feeScheduleRepository.findById(feeScheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Fee schedule not found"));

        List<StudentEnrollment> enrollments;

        if (schedule.getTerm() != null) {
            enrollments = studentEnrollmentRepository.findByDepartmentAndAcademicYearAndTermAndStatus(
                    schedule.getDepartment(),
                    schedule.getAcademicYear(),
                    schedule.getTerm(),
                    "ACTIVE"
            );
        } else {
            enrollments = studentEnrollmentRepository.findByDepartmentAndAcademicYearAndStatus(
                    schedule.getDepartment(),
                    schedule.getAcademicYear(),
                    "ACTIVE"
            );
        }

        int created = 0;

        for (StudentEnrollment enrollment : enrollments) {
            if (schedule.getProgramOrClass() != null && !schedule.getProgramOrClass().isBlank()) {
                if (!schedule.getProgramOrClass().equalsIgnoreCase(enrollment.getProgramOrClass())) {
                    continue;
                }
            }

            boolean alreadyExists = studentChargeRepository.existsByStudentAndFeeSchedule(
                    enrollment.getStudent(),
                    schedule
            );

            if (alreadyExists) {
                continue;
            }

            StudentCharge charge = new StudentCharge();
            charge.setStudent(enrollment.getStudent());
            charge.setDepartment(enrollment.getDepartment());
            charge.setAcademicYear(schedule.getAcademicYear());
            charge.setTerm(schedule.getTerm());
            charge.setFeeSchedule(schedule);
            charge.setServiceCategory(schedule.getFeeType());
            charge.setChargeDate(LocalDate.now());
            charge.setDueDate(schedule.getDueDate());
            charge.setAmount(schedule.getAmount());
            charge.setOriginalAmount(schedule.getAmount());
            charge.setNetAmount(schedule.getAmount());
            charge.setPaidAmount(BigDecimal.ZERO);
            charge.setStatus("POSTED");
            charge.setAccountingStatus("NOT_SUBMITTED");

            studentChargeRepository.save(charge);
            glIntegrationService.postStudentChargeSafely(charge.getId());
            created++;
        }

        auditService.log(
                "FEE_SCHEDULE",
                schedule.getId(),
                "GENERATE_CHARGES",
                null,
                String.valueOf(created),
                "Generated student charges from fee schedule"
        );

        return created;
    }

    @Transactional
    public StudentPaymentPlan createPaymentPlan(
            UUID studentId,
            BigDecimal totalDebt,
            BigDecimal downPayment,
            BigDecimal installmentAmount,
            String frequency,
            LocalDate firstDueDate,
            Integer numberOfInstallments,
            String responsibleOfficer,
            String notes
    ) {
        requireEducationPrivilege();

        if (totalDebt == null || installmentAmount == null || firstDueDate == null || numberOfInstallments == null) {
            throw new IllegalArgumentException("Payment plan fields are required.");
        }

        if (numberOfInstallments <= 0) {
            throw new IllegalArgumentException("Number of installments must be greater than zero.");
        }

        Student student = studentChargeRepository.findAll()
                .stream()
                .filter(charge -> charge.getStudent().getId().equals(studentId))
                .map(StudentCharge::getStudent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Student not found or has no charges"));

        BigDecimal down = nullSafe(downPayment);
        BigDecimal installmentsTotal = installmentAmount.multiply(BigDecimal.valueOf(numberOfInstallments));
        BigDecimal plannedTotal = down.add(installmentsTotal);

        if (plannedTotal.compareTo(totalDebt) != 0) {
            throw new IllegalArgumentException("Down payment plus installments must equal total debt.");
        }

        StudentPaymentPlan plan = new StudentPaymentPlan();
        plan.setStudent(student);
        plan.setDepartment(student.getDepartment());
        plan.setTotalDebt(totalDebt);
        plan.setDownPayment(down);
        plan.setInstallmentAmount(installmentAmount);
        plan.setFrequency(frequency != null ? frequency : "MONTHLY");
        plan.setFirstDueDate(firstDueDate);
        plan.setNumberOfInstallments(numberOfInstallments);
        plan.setResponsibleOfficer(responsibleOfficer);
        plan.setNotes(notes);
        plan.setApprovalStatus("PENDING");
        plan.setStatus("ACTIVE");

        studentPaymentPlanRepository.save(plan);

        List<PaymentPlanInstallment> installments = new ArrayList<>();

        for (int i = 1; i <= numberOfInstallments; i++) {
            LocalDate dueDate = switch (plan.getFrequency().toUpperCase()) {
                case "WEEKLY" -> firstDueDate.plusWeeks(i - 1);
                case "MONTHLY" -> firstDueDate.plusMonths(i - 1);
                case "QUARTERLY" -> firstDueDate.plusMonths(3L * (i - 1));
                default -> throw new IllegalArgumentException("Unsupported frequency: " + plan.getFrequency());
            };

            PaymentPlanInstallment installment = new PaymentPlanInstallment();
            installment.setPaymentPlan(plan);
            installment.setInstallmentNumber(i);
            installment.setDueDate(dueDate);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(BigDecimal.ZERO);
            installment.setStatus("SCHEDULED");

            installments.add(installment);
        }

        paymentPlanInstallmentRepository.saveAll(installments);

        auditService.log(
                "STUDENT_PAYMENT_PLAN",
                plan.getId(),
                "CREATE_PLAN",
                null,
                totalDebt.toPlainString(),
                notes
        );

        return plan;
    }

    private void requireEducationPrivilege() {
        if (!currentUserService.hasPrivilege("EDUCATION_FINANCE_MANAGE")) {
            throw new AccessDeniedException("Current user cannot manage education finance operations.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}