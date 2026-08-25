// src/main/java/com/institution/finance/service/PatientBillingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.InsuranceClaim;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.PaymentAllocation;
import cm.ndicsonlabs.bossapp.repository.InsuranceClaimRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentAllocationRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class PatientBillingService {

    private final PatientChargeRepository patientChargeRepository;
    private final InsuranceClaimRepository insuranceClaimRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    public PatientBillingService(
            PatientChargeRepository patientChargeRepository,
            InsuranceClaimRepository insuranceClaimRepository,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository
    ) {
        this.patientChargeRepository = patientChargeRepository;
        this.insuranceClaimRepository = insuranceClaimRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
    }

    @Transactional
    public Payment recordPatientPayment(PatientCharge charge, BigDecimal amount, String payer, LocalDate paymentDate) {
        validateAmount(amount);

        if (amount.compareTo(charge.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining patient charge balance.");
        }

        Payment payment = createPayment(
                amount,
                payer,
                paymentDate,
                "IN",
                "CASH",
                charge.getDepartment()
        );

        createAllocation(payment, "PATIENT_CHARGE", charge.getId(), amount);

        charge.setPaidAmount(charge.getPaidAmount().add(amount));
        charge.setStatus(charge.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID");
        patientChargeRepository.save(charge);

        return payment;
    }

    @Transactional
    public Payment recordInsuranceClaimPayment(InsuranceClaim claim, BigDecimal amount, String payer, LocalDate paymentDate) {
        validateAmount(amount);

        if (amount.compareTo(claim.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining insurance claim balance.");
        }

        Payment payment = createPayment(
                amount,
                payer,
                paymentDate,
                "IN",
                "INSURANCE_RECEIPT",
                claim.getPatientAccount().getDepartment()
        );

        createAllocation(payment, "INSURANCE_CLAIM", claim.getId(), amount);

        claim.setPaidAmount(claim.getPaidAmount().add(amount));
        claim.setStatus(claim.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID");
        insuranceClaimRepository.save(claim);

        return payment;
    }

    private Payment createPayment(
            BigDecimal amount,
            String payerOrPayee,
            LocalDate paymentDate,
            String direction,
            String method,
            cm.ndicsonlabs.bossapp.domain.Department department
    ) {
        Payment payment = new Payment();
        payment.setPaymentNumber("PMT-" + UUID.randomUUID());
        payment.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        payment.setAmount(amount);
        payment.setUnallocatedAmount(BigDecimal.ZERO);
        payment.setDirection(direction);
        payment.setPayerOrPayee(payerOrPayee);
        payment.setMethod(method);
        payment.setStatus("POSTED");
        payment.setDepartment(department);
        return paymentRepository.save(payment);
    }

    private void createAllocation(Payment payment, String targetType, UUID targetId, BigDecimal amount) {
        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setPayment(payment);
        allocation.setTargetType(targetType);
        allocation.setTargetId(targetId);
        allocation.setAmount(amount);
        paymentAllocationRepository.save(allocation);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
    }
}