// src/main/java/com/institution/finance/service/AgingReportService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.dto.AgingLine;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class AgingReportService {

    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;

    public AgingReportService(
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository
    ) {
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
    }

    public List<AgingLine> studentFeeAging(LocalDate asOf) {
        return studentChargeRepository.findAll()
                .stream()
                .filter(charge -> charge.getRemainingAmount() != null)
                .filter(charge -> charge.getRemainingAmount().signum() > 0)
                .map(charge -> toAgingLine(
                        charge.getStudent().getFullName(),
                        charge.getId().toString(),
                        firstNonNull(charge.getDueDate(), charge.getChargeDate()),
                        charge.getAmount(),
                        charge.getPaidAmount(),
                        charge.getRemainingAmount(),
                        asOf
                ))
                .sorted(Comparator.comparing(AgingLine::getDueDate))
                .toList();
    }

    public List<AgingLine> patientDebtAging(LocalDate asOf) {
        return patientChargeRepository.findAll()
                .stream()
                .filter(charge -> charge.getRemainingAmount() != null)
                .filter(charge -> charge.getRemainingAmount().signum() > 0)
                .map(charge -> toAgingLine(
                        charge.getPatientAccount().getFullName(),
                        charge.getServiceCategory() != null ? charge.getServiceCategory() : charge.getId().toString(),
                        firstNonNull(charge.getDueDate(), charge.getChargeDate()),
                        charge.getAmount(),
                        charge.getPaidAmount(),
                        charge.getRemainingAmount(),
                        asOf
                ))
                .sorted(Comparator.comparing(AgingLine::getDueDate))
                .toList();
    }

    public List<AgingLine> supplierCreditAging(LocalDate asOf) {
        return supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getRemainingAmount() != null)
                .filter(invoice -> invoice.getRemainingAmount().signum() > 0)
                .map(invoice -> toAgingLine(
                        invoice.getSupplier().getName(),
                        invoice.getInvoiceNumber(),
                        firstNonNull(invoice.getDueDate(), invoice.getInvoiceDate()),
                        invoice.getTotalAmount(),
                        invoice.getPaidAmount(),
                        invoice.getRemainingAmount(),
                        asOf
                ))
                .sorted(Comparator.comparing(AgingLine::getDueDate))
                .toList();
    }

    private AgingLine toAgingLine(
            String entityName,
            String reference,
            LocalDate dueDate,
            BigDecimal originalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            LocalDate asOf
    ) {
        long overdueDays = 0;
        String bucket = "Current";

        if (dueDate != null && dueDate.isBefore(asOf)) {
            overdueDays = ChronoUnit.DAYS.between(dueDate, asOf);
            bucket = bucket(overdueDays);
        }

        return new AgingLine(
                entityName,
                reference,
                dueDate != null ? dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE) : "",
                originalAmount,
                paidAmount,
                outstandingAmount,
                overdueDays,
                bucket
        );
    }

    private String bucket(long overdueDays) {
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

    private LocalDate firstNonNull(LocalDate primary, LocalDate fallback) {
        return primary != null ? primary : fallback;
    }
}