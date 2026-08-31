// src/main/java/com/institution/finance/service/FormalReportingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.ReportPackRun;
import cm.ndicsonlabs.bossapp.dto.StatementLine;
import cm.ndicsonlabs.bossapp.repository.ReportPackRunRepository;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FormalReportingService {

    private final ConsolidatedStatementService statementService;
    private final JasperReportService jasperReportService;
    private final ReportPackRunRepository reportPackRunRepository;
    private final CurrentUserService currentUserService;

    public FormalReportingService(
            ConsolidatedStatementService statementService,
            JasperReportService jasperReportService,
            ReportPackRunRepository reportPackRunRepository,
            CurrentUserService currentUserService
    ) {
        this.statementService = statementService;
        this.jasperReportService = jasperReportService;
        this.reportPackRunRepository = reportPackRunRepository;
        this.currentUserService = currentUserService;
    }

    public byte[] generateBalanceSheet(UUID departmentId, LocalDate asOf, String format) {
        List<StatementLine> lines = statementService.balanceSheet(departmentId, asOf);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", departmentId != null
                ? "Departmental Statement of Financial Position"
                : "Consolidated Statement of Financial Position");
        parameters.put("AsOfDate", asOf.toString());

        JasperPrint print = jasperReportService.fill(
                "/reports/statement_of_financial_position.jrxml",
                parameters,
                lines
        );

        byte[] output = export(print, format);

        logReportPack("STATEMENT_OF_FINANCIAL_POSITION", format, "statement_of_financial_position." + extension(format));

        return output;
    }

    public byte[] generateStatementOfActivity(UUID departmentId, LocalDate from, LocalDate to, String format) {
        List<StatementLine> lines = statementService.statementOfActivity(departmentId, from, to);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", departmentId != null
                ? "Departmental Statement of Activity"
                : "Consolidated Statement of Activity");
        parameters.put("FromDate", from != null ? from.toString() : "");
        parameters.put("ToDate", to != null ? to.toString() : "");

        JasperPrint print = jasperReportService.fill(
                "/reports/statement_of_activity.jrxml",
                parameters,
                lines
        );

        byte[] output = export(print, format);

        logReportPack("STATEMENT_OF_ACTIVITY", format, "statement_of_activity." + extension(format));

        return output;
    }

    private byte[] export(JasperPrint print, String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> jasperReportService.exportPdf(print);
            case "CSV" -> jasperReportService.exportCsv(print);
            case "XLSX", "EXCEL" -> jasperReportService.exportXlsx(print);
            default -> throw new IllegalArgumentException("Unsupported report format: " + format);
        };
    }

    private void logReportPack(String reportCode, String format, String fileName) {
        ReportPackRun run = new ReportPackRun();
        run.setReportCode(reportCode);
        run.setOutputFormat(format);
        run.setFileName(fileName);
        run.setStatus("SUCCESS");
        run.setGeneratedBy(currentUserService.username());

        reportPackRunRepository.save(run);
    }

    private String extension(String format) {
        return switch (format.toUpperCase()) {
            case "PDF" -> "pdf";
            case "CSV" -> "csv";
            case "XLSX", "EXCEL" -> "xlsx";
            default -> "dat";
        };
    }
}