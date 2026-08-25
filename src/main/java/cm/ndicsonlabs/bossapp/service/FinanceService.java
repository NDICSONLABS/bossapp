// src/main/java/com/institution/finance/service/FinanceService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.PaymentAllocation;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.PaymentAllocationRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FinanceService {

    private final StudentChargeRepository studentChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAllocationRepository paymentAllocationRepository;

    public FinanceService(
            StudentChargeRepository studentChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository,
            PaymentAllocationRepository paymentAllocationRepository
    ) {
        this.studentChargeRepository = studentChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.paymentAllocationRepository = paymentAllocationRepository;
    }

    @Transactional
    public Payment recordStudentPayment(StudentCharge charge, BigDecimal amount, String payer, LocalDate paymentDate) {
        validatePaymentAmount(amount);

        if (amount.compareTo(charge.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining student charge balance.");
        }

        Payment payment = new Payment();
        payment.setPaymentNumber("PMT-" + UUID.randomUUID());
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setUnallocatedAmount(BigDecimal.ZERO);
        payment.setDirection("IN");
        payment.setPayerOrPayee(payer);
        payment.setMethod("CASH");
        payment.setStatus("POSTED");
        payment.setDepartment(charge.getDepartment());
        paymentRepository.save(payment);

        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setPayment(payment);
        allocation.setTargetType("STUDENT_CHARGE");
        allocation.setTargetId(charge.getId());
        allocation.setAmount(amount);
        paymentAllocationRepository.save(allocation);

        charge.setPaidAmount(charge.getPaidAmount().add(amount));
        charge.setStatus(charge.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID");
        studentChargeRepository.save(charge);

        return payment;
    }

    @Transactional
    public Payment recordSupplierPayment(SupplierInvoice invoice, BigDecimal amount, String payee, LocalDate paymentDate) {
        validatePaymentAmount(amount);

        if (amount.compareTo(invoice.getRemainingAmount()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining supplier invoice balance.");
        }

        Payment payment = new Payment();
        payment.setPaymentNumber("PMT-" + UUID.randomUUID());
        payment.setPaymentDate(paymentDate);
        payment.setAmount(amount);
        payment.setUnallocatedAmount(BigDecimal.ZERO);
        payment.setDirection("OUT");
        payment.setPayerOrPayee(payee);
        payment.setMethod("BANK_TRANSFER");
        payment.setStatus("POSTED");
        payment.setDepartment(invoice.getDepartment());
        paymentRepository.save(payment);

        PaymentAllocation allocation = new PaymentAllocation();
        allocation.setPayment(payment);
        allocation.setTargetType("SUPPLIER_INVOICE");
        allocation.setTargetId(invoice.getId());
        allocation.setAmount(amount);
        paymentAllocationRepository.save(allocation);

        invoice.setPaidAmount(invoice.getPaidAmount().add(amount));
        invoice.setStatus(invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0 ? "PAID" : "PARTIALLY_PAID");
        supplierInvoiceRepository.save(invoice);

        return payment;
    }

    private void validatePaymentAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
    }
}