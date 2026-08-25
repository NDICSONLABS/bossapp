// src/main/java/com/institution/finance/service/ReportControlService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.ReportRun;
import cm.ndicsonlabs.bossapp.domain.ReportTemplate;
import cm.ndicsonlabs.bossapp.repository.ReportRunRepository;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportControlService {

    private final AgingReportService agingReportService;
    private final CashFlowForecastService cashFlowForecastService;
    private final JasperReportService jasperReportService;
    private final ReportRunRepository reportRunRepository;

    public ReportControlService(
            AgingReportService agingReportService,
            CashFlowForecastService cashFlowForecastService,
            JasperReportService jasperReportService,
            ReportRunRepository reportRunRepository
    ) {
        this.agingReportService = agingReportService;
        this.cashFlowForecastService = cashFlowForecastService;
        this.jasperReportService = jasperReportService;
        this.reportRunRepository = reportRunRepository;
    }

    @Transactional
    public byte[] generate(ReportTemplate template, String format, LocalDate asOf) {
        LocalDate effectiveAsOf = asOf != null ? asOf : LocalDate.now();

        try {
            ReportData reportData = buildReportData(template, effectiveAsOf);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", reportData.title());
            parameters.put("AsOfDate", effectiveAsOf.toString());

            JasperPrint print = jasperReportService.fill(
                    template.getJasperResource(),
                    parameters,
                    reportData.data()
            );

            byte[] output = switch (format.toUpperCase()) {
                case "PDF" -> jasperReportService.exportPdf(print);
                case "CSV" -> jasperReportService.exportCsv(print);
                case "XLSX", "EXCEL" -> jasperReportService.exportXlsx(print);
                default -> throw new IllegalArgumentException("Unsupported report format: " + format);
            };

            logRun(template, format, "asOf=" + effectiveAsOf, "SUCCESS", null);

            return output;
        } catch (Exception ex) {
            logRun(template, format, "asOf=" + effectiveAsOf, "ERROR", ex.getMessage());
            throw new RuntimeException("Report generation failed: " + ex.getMessage(), ex);
        }
    }

    private ReportData buildReportData(ReportTemplate template, LocalDate asOf) {
        return switch (template.getCode()) {
            case "STUDENT_FEE_AGING" -> new ReportData(
                    "Student Fee Aging",
                    template.getJasperResource(),
                    agingReportService.studentFeeAging(asOf)
            );

            case "PATIENT_DEBT_AGING" -> new ReportData(
                    "Patient Debt Aging",
                    template.getJasperResource(),
                    agingReportService.patientDebtAging(asOf)
            );

            case "SUPPLIER_CREDIT_AGING" -> new ReportData(
                    "Supplier Credit Aging",
                    template.getJasperResource(),
                    agingReportService.supplierCreditAging(asOf)
            );

            case "CASH_FLOW_FORECAST" -> new ReportData(
                    "Cash Flow Forecast",
                    template.getJasperResource(),
                    cashFlowForecastService.forecast(asOf, asOf.plusDays(90))
            );

            default -> throw new IllegalArgumentException("Unknown report template code: " + template.getCode());
        };
    }

    private void logRun(
            ReportTemplate template,
            String format,
            String parameters,
            String status,
            String message
    ) {
        ReportRun run = new ReportRun();
        run.setReportTemplate(template);
        run.setRunByUsername(currentUsername());
        run.setParameters(parameters);
        run.setOutputFormat(format);
        run.setStatus(status);
        run.setMessage(message);

        reportRunRepository.save(run);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "system";
        }

        return authentication.getName();
    }

    private record ReportData(String title, String jasperResource, List<?> data) {
    }
}