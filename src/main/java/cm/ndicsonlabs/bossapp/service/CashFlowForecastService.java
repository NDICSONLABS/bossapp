// src/main/java/com/institution/finance/service/CashFlowForecastService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.CashFlowAdjustment;
import cm.ndicsonlabs.bossapp.repository.CashFlowAdjustmentRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import cm.ndicsonlabs.bossapp.dto.CashFlowLine;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CashFlowForecastService {

    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final CashFlowAdjustmentRepository cashFlowAdjustmentRepository;

    public CashFlowForecastService(
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            CashFlowAdjustmentRepository cashFlowAdjustmentRepository
    ) {
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.cashFlowAdjustmentRepository = cashFlowAdjustmentRepository;
    }

    public List<CashFlowLine> forecast(LocalDate start, LocalDate end) {
        List<CashFlowLine> lines = new ArrayList<>();

        studentChargeRepository.findAll()
                .stream()
                .filter(charge -> charge.getRemainingAmount() != null)
                .filter(charge -> charge.getRemainingAmount().signum() > 0)
                .forEach(charge -> {
                    LocalDate date = charge.getDueDate() != null ? charge.getDueDate() : charge.getChargeDate();

                    if (isWithinRange(date, start, end)) {
                        lines.add(new CashFlowLine(
                                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                "Student fee receivable: " + charge.getStudent().getFullName(),
                                "INFLOW",
                                charge.getRemainingAmount(),
                                "STUDENT_FEES"
                        ));
                    }
                });

        patientChargeRepository.findAll()
                .stream()
                .filter(charge -> charge.getRemainingAmount() != null)
                .filter(charge -> charge.getRemainingAmount().signum() > 0)
                .forEach(charge -> {
                    LocalDate date = charge.getDueDate() != null ? charge.getDueDate() : charge.getChargeDate();

                    if (isWithinRange(date, start, end)) {
                        lines.add(new CashFlowLine(
                                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                "Patient charge receivable: " + charge.getPatientAccount().getFullName(),
                                "INFLOW",
                                charge.getRemainingAmount(),
                                "PATIENT_SERVICES"
                        ));
                    }
                });

        supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getRemainingAmount() != null)
                .filter(invoice -> invoice.getRemainingAmount().signum() > 0)
                .forEach(invoice -> {
                    LocalDate date = invoice.getDueDate() != null ? invoice.getDueDate() : invoice.getInvoiceDate();

                    if (isWithinRange(date, start, end)) {
                        lines.add(new CashFlowLine(
                                date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                "Supplier credit payable: " + invoice.getSupplier().getName(),
                                "OUTFLOW",
                                invoice.getRemainingAmount(),
                                "SUPPLIER_CREDITS"
                        ));
                    }
                });

        cashFlowAdjustmentRepository.findByAdjustmentDateBetween(start, end)
                .stream()
                .filter(adjustment -> "APPROVED".equals(adjustment.getStatus()))
                .filter(adjustment -> adjustment.getAmount() != null)
                .filter(adjustment -> adjustment.getAmount().signum() > 0)
                .forEach(adjustment -> lines.add(toCashFlowLine(adjustment)));

        return lines.stream()
                .sorted(Comparator.comparing(CashFlowLine::getFlowDate))
                .toList();
    }

    private CashFlowLine toCashFlowLine(CashFlowAdjustment adjustment) {
        return new CashFlowLine(
                adjustment.getAdjustmentDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                adjustment.getDescription(),
                adjustment.getDirection(),
                adjustment.getAmount(),
                "MANUAL_ADJUSTMENT"
        );
    }

    private boolean isWithinRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) {
            return false;
        }

        return !date.isBefore(start) && !date.isAfter(end);
    }
}