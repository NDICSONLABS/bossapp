// src/main/java/com/institution/finance/service/GlIntegrationService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.*;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.GlIntegrationLogRepository;
import cm.ndicsonlabs.bossapp.repository.GlSettingRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SubmissionTransactionRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class GlIntegrationService {

    private final AccountingPostingService postingService;
    private final AccountingEntryRepository entryRepository;
    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SubmissionTransactionRepository submissionTransactionRepository;
    private final GlIntegrationLogRepository logRepository;
    private final GlSettingRepository settingRepository;

    public GlIntegrationService(
            AccountingPostingService postingService,
            AccountingEntryRepository entryRepository,
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository,
            SubmissionTransactionRepository submissionTransactionRepository,
            GlIntegrationLogRepository logRepository,
            GlSettingRepository settingRepository
    ) {
        this.postingService = postingService;
        this.entryRepository = entryRepository;
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.submissionTransactionRepository = submissionTransactionRepository;
        this.logRepository = logRepository;
        this.settingRepository = settingRepository;
    }

    public int postAllStudentCharges() {
        List<StudentCharge> charges = studentChargeRepository.findByGlStatusIn(
                List.of("NOT_POSTED", "ERROR")
        );

        for (StudentCharge charge : charges) {
            postStudentChargeSafely(charge.getId());
        }

        return charges.size();
    }

    public int postAllPatientCharges() {
        List<PatientCharge> charges = patientChargeRepository.findByGlStatusIn(
                List.of("NOT_POSTED", "ERROR")
        );

        for (PatientCharge charge : charges) {
            postPatientChargeSafely(charge.getId());
        }

        return charges.size();
    }

    public int postAllSupplierInvoices() {
        List<SupplierInvoice> invoices = supplierInvoiceRepository.findByGlStatusIn(
                List.of("NOT_POSTED", "ERROR")
        );

        for (SupplierInvoice invoice : invoices) {
            postSupplierInvoiceSafely(invoice.getId());
        }

        return invoices.size();
    }

    public int postAllPayments() {
        List<Payment> payments = paymentRepository.findByGlStatusIn(
                List.of("NOT_POSTED", "ERROR")
        );

        for (Payment payment : payments) {
            postPaymentSafely(payment.getId());
        }

        return payments.size();
    }

    public void postStudentChargeSafely(UUID studentChargeId) {
        try {
            if (entryRepository.existsBySourceTypeAndSourceId("STUDENT_CHARGE", studentChargeId)) {
                markStudentChargePosted(studentChargeId);
                log("STUDENT_CHARGE", studentChargeId, "POST", "SKIPPED_ALREADY_POSTED", null);
                return;
            }

            postingService.postStudentCharge(studentChargeId);
            markStudentChargePosted(studentChargeId);
            log("STUDENT_CHARGE", studentChargeId, "POST", "SUCCESS", null);
        } catch (Exception ex) {
            markStudentChargeError(studentChargeId, ex.getMessage());
            log("STUDENT_CHARGE", studentChargeId, "POST", "ERROR", ex.getMessage());
        }
    }

    public void postPatientChargeSafely(UUID patientChargeId) {
        try {
            if (entryRepository.existsBySourceTypeAndSourceId("PATIENT_CHARGE", patientChargeId)) {
                markPatientChargePosted(patientChargeId);
                log("PATIENT_CHARGE", patientChargeId, "POST", "SKIPPED_ALREADY_POSTED", null);
                return;
            }

            postingService.postPatientCharge(patientChargeId);
            markPatientChargePosted(patientChargeId);
            log("PATIENT_CHARGE", patientChargeId, "POST", "SUCCESS", null);
        } catch (Exception ex) {
            markPatientChargeError(patientChargeId, ex.getMessage());
            log("PATIENT_CHARGE", patientChargeId, "POST", "ERROR", ex.getMessage());
        }
    }

    public void postSupplierInvoiceSafely(UUID supplierInvoiceId) {
        try {
            if (entryRepository.existsBySourceTypeAndSourceId("SUPPLIER_INVOICE", supplierInvoiceId)) {
                markSupplierInvoicePosted(supplierInvoiceId);
                log("SUPPLIER_INVOICE", supplierInvoiceId, "POST", "SKIPPED_ALREADY_POSTED", null);
                return;
            }

            postingService.postSupplierInvoice(supplierInvoiceId);
            markSupplierInvoicePosted(supplierInvoiceId);
            log("SUPPLIER_INVOICE", supplierInvoiceId, "POST", "SUCCESS", null);
        } catch (Exception ex) {
            markSupplierInvoiceError(supplierInvoiceId, ex.getMessage());
            log("SUPPLIER_INVOICE", supplierInvoiceId, "POST", "ERROR", ex.getMessage());
        }
    }

    public void postPaymentSafely(UUID paymentId) {
        try {
            if (entryRepository.existsBySourceTypeAndSourceId("PAYMENT", paymentId)) {
                markPaymentPosted(paymentId);
                log("PAYMENT", paymentId, "POST", "SKIPPED_ALREADY_POSTED", null);
                return;
            }

            postingService.postPayment(paymentId);
            markPaymentPosted(paymentId);
            log("PAYMENT", paymentId, "POST", "SUCCESS", null);
        } catch (Exception ex) {
            markPaymentError(paymentId, ex.getMessage());
            log("PAYMENT", paymentId, "POST", "ERROR", ex.getMessage());
        }
    }

    public void postSubmissionTransactions(UUID submissionId) {
        if (!settingEnabled("AUTO_POST_ON_SUBMISSION_ACCEPT", true)) {
            return;
        }

        List<SubmissionTransaction> lines = submissionTransactionRepository.findBySubmissionId(submissionId);

        for (SubmissionTransaction line : lines) {
            switch (line.getTargetType()) {
                case "STUDENT_CHARGE" -> postStudentChargeSafely(line.getTargetId());
                case "PATIENT_CHARGE" -> postPatientChargeSafely(line.getTargetId());
                case "SUPPLIER_INVOICE" -> postSupplierInvoiceSafely(line.getTargetId());
                case "PAYMENT" -> postPaymentSafely(line.getTargetId());
                default -> log(
                        "SUBMISSION_TRANSACTION",
                        line.getId(),
                        "POST",
                        "SKIPPED_UNKNOWN_TARGET",
                        "Unknown submission target type: " + line.getTargetType()
                );
            }
        }
    }

    public long countUnpostedForPeriod(AccountingPeriod period) {
        List<String> statuses = List.of("NOT_POSTED", "ERROR");

        long studentCharges = studentChargeRepository.countByChargeDateBetweenAndGlStatusIn(
                period.getStartDate(),
                period.getEndDate(),
                statuses
        );

        long patientCharges = patientChargeRepository.countByChargeDateBetweenAndGlStatusIn(
                period.getStartDate(),
                period.getEndDate(),
                statuses
        );

        long supplierInvoices = supplierInvoiceRepository.countByInvoiceDateBetweenAndGlStatusIn(
                period.getStartDate(),
                period.getEndDate(),
                statuses
        );

        long payments = paymentRepository.countByPaymentDateBetweenAndGlStatusIn(
                period.getStartDate(),
                period.getEndDate(),
                statuses
        );

        return studentCharges + patientCharges + supplierInvoices + payments;
    }

    private void markStudentChargePosted(UUID id) {
        studentChargeRepository.findById(id).ifPresent(charge -> {
            charge.setGlStatus("POSTED");
            charge.setGlError(null);
            charge.setGlPostedAt(Instant.now());
            studentChargeRepository.save(charge);
        });
    }

    private void markStudentChargeError(UUID id, String error) {
        studentChargeRepository.findById(id).ifPresent(charge -> {
            charge.setGlStatus("ERROR");
            charge.setGlError(error);
            studentChargeRepository.save(charge);
        });
    }

    private void markPatientChargePosted(UUID id) {
        patientChargeRepository.findById(id).ifPresent(charge -> {
            charge.setGlStatus("POSTED");
            charge.setGlError(null);
            charge.setGlPostedAt(Instant.now());
            patientChargeRepository.save(charge);
        });
    }

    private void markPatientChargeError(UUID id, String error) {
        patientChargeRepository.findById(id).ifPresent(charge -> {
            charge.setGlStatus("ERROR");
            charge.setGlError(error);
            patientChargeRepository.save(charge);
        });
    }

    private void markSupplierInvoicePosted(UUID id) {
        supplierInvoiceRepository.findById(id).ifPresent(invoice -> {
            invoice.setGlStatus("POSTED");
            invoice.setGlError(null);
            invoice.setGlPostedAt(Instant.now());
            supplierInvoiceRepository.save(invoice);
        });
    }

    private void markSupplierInvoiceError(UUID id, String error) {
        supplierInvoiceRepository.findById(id).ifPresent(invoice -> {
            invoice.setGlStatus("ERROR");
            invoice.setGlError(error);
            supplierInvoiceRepository.save(invoice);
        });
    }

    private void markPaymentPosted(UUID id) {
        paymentRepository.findById(id).ifPresent(payment -> {
            payment.setGlStatus("POSTED");
            payment.setGlError(null);
            payment.setGlPostedAt(Instant.now());
            paymentRepository.save(payment);
        });
    }

    private void markPaymentError(UUID id, String error) {
        paymentRepository.findById(id).ifPresent(payment -> {
            payment.setGlStatus("ERROR");
            payment.setGlError(error);
            paymentRepository.save(payment);
        });
    }

    private void log(String sourceType, UUID sourceId, String action, String status, String message) {
        GlIntegrationLog logEntry = new GlIntegrationLog();
        logEntry.setSourceType(sourceType);
        logEntry.setSourceId(sourceId);
        logEntry.setAction(action);
        logEntry.setStatus(status);
        logEntry.setMessage(message);

        logRepository.save(logEntry);
    }

    private boolean settingEnabled(String key, boolean defaultValue) {
        return settingRepository.findById(key)
                .map(setting -> "TRUE".equalsIgnoreCase(setting.getValue()))
                .orElse(defaultValue);
    }
}