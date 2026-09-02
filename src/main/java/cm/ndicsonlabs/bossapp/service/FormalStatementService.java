package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.dto.StatementLine;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FormalStatementService {

    private final ConsolidatedStatementService statementService;
    private final JasperReportService jasperReportService;

    public FormalStatementService(
            ConsolidatedStatementService statementService,
            JasperReportService jasperReportService
    ) {
        this.statementService = statementService;
        this.jasperReportService = jasperReportService;
    }

    public byte[] generate(
            String reportType,
            UUID departmentId,
            LocalDate asOf,
            LocalDate from,
            LocalDate to,
            String format
    ) {
        JasperPrint print;

        if ("BALANCE_SHEET".equals(reportType)) {
            List<StatementLine> lines = statementService.balanceSheet(departmentId, asOf);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", departmentId != null
                    ? "Departmental Statement of Financial Position"
                    : "Consolidated Statement of Financial Position");
            parameters.put("AsOfDate", asOf.toString());

            print = jasperReportService.fill(
                    "/reports/statement_of_financial_position.jrxml",
                    parameters,
                    lines
            );
        } else if ("ACTIVITY".equals(reportType)) {
            List<StatementLine> lines = statementService.statementOfActivity(departmentId, from, to);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("ReportTitle", departmentId != null
                    ? "Departmental Statement of Activity"
                    : "Consolidated Statement of Activity");
            parameters.put("FromDate", from != null ? from.toString() : "");
            parameters.put("ToDate", to != null ? to.toString() : "");

            print = jasperReportService.fill(
                    "/reports/statement_of_activity.jrxml",
                    parameters,
                    lines
            );
        } else {
            throw new IllegalArgumentException("Unsupported formal statement type: " + reportType);
        }

        return switch (format.toUpperCase()) {
            case "PDF" -> jasperReportService.exportPdf(print);
            case "CSV" -> jasperReportService.exportCsv(print);
            case "XLSX", "EXCEL" -> jasperReportService.exportXlsx(print);
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }
}