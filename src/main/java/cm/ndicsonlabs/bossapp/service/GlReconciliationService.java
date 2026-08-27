// src/main/java/com/institution/finance/service/GlReconciliationService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.GlReconciliation;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.GlReconciliationRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GlReconciliationService {

    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final AccountingEntryLineRepository entryLineRepository;
    private final GlReconciliationRepository reconciliationRepository;

    public GlReconciliationService(
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository,
            AccountMappingRepository accountMappingRepository,
            AccountingEntryLineRepository entryLineRepository,
            GlReconciliationRepository reconciliationRepository
    ) {
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.entryLineRepository = entryLineRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    @Transactional
    public List<GlReconciliation> runReconciliation(LocalDate asOf) {
        List<GlReconciliation> results = new ArrayList<>();

        results.add(reconcileStudentCharges(asOf));
        results.add(reconcilePatientCharges(asOf));
        results.add(reconcileSupplierInvoices(asOf));
        results.add(reconcileIncomingPayments(asOf));
        results.add(reconcileOutgoingPayments(asOf));

        return reconciliationRepository.saveAll(results);
    }

    private GlReconciliation reconcileStudentCharges(LocalDate asOf) {
        BigDecimal subledger = studentChargeRepository.findAll()
                .stream()
                .filter(charge -> isOnOrBefore(charge.getChargeDate(), asOf))
                .filter(charge -> !"CANCELLED".equals(charge.getStatus()))
                .map(charge -> nullSafe(charge.getNetAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID accountId = accountMappingRepository.findByMappingType("STUDENT_CHARGE_AR")
                .map(AccountMapping::getAccountCode)
                .map(account -> account.getId())
                .orElse(null);

        BigDecimal gl = sumDebitLines(accountId, "STUDENT_CHARGE", asOf);

        return buildReconciliation(asOf, "STUDENT_CHARGE", subledger, gl);
    }

    private GlReconciliation reconcilePatientCharges(LocalDate asOf) {
        BigDecimal subledger = patientChargeRepository.findAll()
                .stream()
                .filter(charge -> isOnOrBefore(charge.getChargeDate(), asOf))
                .filter(charge -> !"CANCELLED".equals(charge.getStatus()))
                .map(charge -> nullSafe(charge.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID accountId = accountMappingRepository.findByMappingType("PATIENT_CHARGE_AR")
                .map(AccountMapping::getAccountCode)
                .map(account -> account.getId())
                .orElse(null);

        BigDecimal gl = sumDebitLines(accountId, "PATIENT_CHARGE", asOf);

        return buildReconciliation(asOf, "PATIENT_CHARGE", subledger, gl);
    }

    private GlReconciliation reconcileSupplierInvoices(LocalDate asOf) {
        BigDecimal subledger = supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> isOnOrBefore(invoice.getInvoiceDate(), asOf))
                .filter(invoice -> !"CANCELLED".equals(invoice.getStatus()))
                .map(invoice -> nullSafe(invoice.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID accountId = accountMappingRepository.findByMappingType("SUPPLIER_INVOICE_AP")
                .map(AccountMapping::getAccountCode)
                .map(account -> account.getId())
                .orElse(null);

        BigDecimal gl = sumCreditLines(accountId, "SUPPLIER_INVOICE", asOf);

        return buildReconciliation(asOf, "SUPPLIER_INVOICE", subledger, gl);
    }

    private GlReconciliation reconcileIncomingPayments(LocalDate asOf) {
        BigDecimal subledger = paymentRepository.findAll()
                .stream()
                .filter(payment -> "IN".equals(payment.getDirection()))
                .filter(payment -> isOnOrBefore(payment.getPaymentDate(), asOf))
                .map(payment -> nullSafe(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID accountId = accountMappingRepository.findByMappingType("PAYMENT_IN_CASH")
                .map(AccountMapping::getAccountCode)
                .map(account -> account.getId())
                .orElse(null);

        BigDecimal gl = sumDebitLines(accountId, "PAYMENT", asOf);

        return buildReconciliation(asOf, "PAYMENT_IN", subledger, gl);
    }

    private GlReconciliation reconcileOutgoingPayments(LocalDate asOf) {
        BigDecimal subledger = paymentRepository.findAll()
                .stream()
                .filter(payment -> "OUT".equals(payment.getDirection()))
                .filter(payment -> isOnOrBefore(payment.getPaymentDate(), asOf))
                .map(payment -> nullSafe(payment.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        UUID accountId = accountMappingRepository.findByMappingType("PAYMENT_OUT_CASH")
                .map(AccountMapping::getAccountCode)
                .map(account -> account.getId())
                .orElse(null);

        BigDecimal gl = sumCreditLines(accountId, "PAYMENT", asOf);

        return buildReconciliation(asOf, "PAYMENT_OUT", subledger, gl);
    }

    private BigDecimal sumDebitLines(UUID accountCodeId, String sourceType, LocalDate asOf) {
        if (accountCodeId == null) {
            return BigDecimal.ZERO;
        }

        return entryLineRepository.findPostedByAccountId(accountCodeId)
                .stream()
                .filter(line -> line.getEntry() != null)
                .filter(line -> sourceType.equals(line.getEntry().getSourceType()))
                .filter(line -> isOnOrBefore(line.getEntry().getEntryDate(), asOf))
                .map(line -> nullSafe(line.getDebit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCreditLines(UUID accountCodeId, String sourceType, LocalDate asOf) {
        if (accountCodeId == null) {
            return BigDecimal.ZERO;
        }

        return entryLineRepository.findPostedByAccountId(accountCodeId)
                .stream()
                .filter(line -> line.getEntry() != null)
                .filter(line -> sourceType.equals(line.getEntry().getSourceType()))
                .filter(line -> isOnOrBefore(line.getEntry().getEntryDate(), asOf))
                .map(line -> nullSafe(line.getCredit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GlReconciliation buildReconciliation(
            LocalDate asOf,
            String sourceType,
            BigDecimal subledger,
            BigDecimal gl
    ) {
        GlReconciliation reconciliation = new GlReconciliation();
        reconciliation.setReconciliationDate(asOf);
        reconciliation.setSourceType(sourceType);
        reconciliation.setSubledgerAmount(subledger);
        reconciliation.setGlAmount(gl);
        reconciliation.setVariance(subledger.subtract(gl));
        reconciliation.setStatus(reconciliation.getVariance().compareTo(BigDecimal.ZERO) == 0 ? "BALANCED" : "VARIANCE");
        reconciliation.setNotes("Automated sub-ledger to GL reconciliation.");

        return reconciliation;
    }

    private boolean isOnOrBefore(LocalDate date, LocalDate asOf) {
        return date != null && !date.isAfter(asOf);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}