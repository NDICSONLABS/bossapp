// src/main/java/com/institution/finance/ui/DepartmentalStatementsView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.dto.StatementLine;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.service.ConsolidatedStatementService;
import cm.ndicsonlabs.bossapp.service.FormalReportingService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import jakarta.annotation.security.PermitAll;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.UUID;

@Route(value = "departmental-statements", layout = MainLayout.class)
@PermitAll
public class DepartmentalStatementsView extends VerticalLayout {

    StreamResourceRegistry registry = VaadinSession.getCurrent().getResourceRegistry();

    private final Grid<StatementLine> balanceGrid = new Grid<>(StatementLine.class);
    private final Grid<StatementLine> activityGrid = new Grid<>(StatementLine.class);

    public DepartmentalStatementsView(
            DepartmentRepository departmentRepository,
            ConsolidatedStatementService statementService,
            FormalReportingService reportingService
    ) {
        ComboBox<Department> departmentBox = new ComboBox<>("Department");
        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);
        departmentBox.setClearButtonVisible(true);

        DatePicker balanceAsOf = new DatePicker("Balance Sheet As Of");
        balanceAsOf.setValue(LocalDate.now());

        DatePicker activityFrom = new DatePicker("Activity From");
        activityFrom.setValue(LocalDate.now().withDayOfYear(1));

        DatePicker activityTo = new DatePicker("Activity To");
        activityTo.setValue(LocalDate.now());

        Button refreshButton = new Button("Refresh Statements", e -> refresh(departmentBox, balanceAsOf, activityFrom, activityTo, statementService));

        balanceGrid.setColumns("section", "line", "amount");
        activityGrid.setColumns("section", "line", "amount");

        refresh(departmentBox, balanceAsOf, activityFrom, activityTo, statementService);

        Anchor balancePdf = statementAnchor("BALANCE_SHEET", "PDF", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);
        Anchor balanceCsv = statementAnchor("BALANCE_SHEET", "CSV", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);
        Anchor balanceXlsx = statementAnchor("BALANCE_SHEET", "XLSX", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);

        Anchor activityPdf = statementAnchor("ACTIVITY", "PDF", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);
        Anchor activityCsv = statementAnchor("ACTIVITY", "CSV", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);
        Anchor activityXlsx = statementAnchor("ACTIVITY", "XLSX", departmentBox, balanceAsOf, activityFrom, activityTo, reportingService);

        add(
                new H2("Departmental and Consolidated Financial Statements"),
                new HorizontalLayout(departmentBox, balanceAsOf, activityFrom, activityTo, refreshButton),

                new H2("Statement of Financial Position"),
                new HorizontalLayout(balancePdf, balanceCsv, balanceXlsx),
                balanceGrid,

                new H2("Statement of Activity"),
                new HorizontalLayout(activityPdf, activityCsv, activityXlsx),
                activityGrid
        );
    }

    private void refresh(
            ComboBox<Department> departmentBox,
            DatePicker balanceAsOf,
            DatePicker activityFrom,
            DatePicker activityTo,
            ConsolidatedStatementService statementService
    ) {
        UUID departmentId = departmentBox.getValue() != null ? departmentBox.getValue().getId() : null;

        balanceGrid.setItems(statementService.balanceSheet(departmentId, balanceAsOf.getValue()));
        activityGrid.setItems(statementService.statementOfActivity(departmentId, activityFrom.getValue(), activityTo.getValue()));
    }

    private Anchor statementAnchor(
            String reportType,
            String format,
            ComboBox<Department> departmentBox,
            DatePicker balanceAsOf,
            DatePicker activityFrom,
            DatePicker activityTo,
            FormalReportingService reportingService
    ) {
        Anchor anchor = new Anchor();
        anchor.getElement().setAttribute("download", true);
        anchor.add(new Button("Download " + format));

        String fileName = reportType.toLowerCase() + "." + switch (format.toUpperCase()) {
            case "PDF" -> "pdf";
            case "CSV" -> "csv";
            default -> "xlsx";
        };

        StreamResource streamResource = new StreamResource(fileName, () -> new ByteArrayInputStream(
                generate(reportType, format, departmentBox, balanceAsOf, activityFrom, activityTo, reportingService)
        ));
        StreamRegistration streamRegistration = registry.registerResource(streamResource);
        anchor.setHref(streamRegistration.getResourceUri().toString());

        return anchor;
    }

    private byte[] generate(
            String reportType,
            String format,
            ComboBox<Department> departmentBox,
            DatePicker balanceAsOf,
            DatePicker activityFrom,
            DatePicker activityTo,
            FormalReportingService reportingService
    ) {
        UUID departmentId = departmentBox.getValue() != null ? departmentBox.getValue().getId() : null;

        if ("BALANCE_SHEET".equals(reportType)) {
            return reportingService.generateBalanceSheet(departmentId, balanceAsOf.getValue(), format);
        }

        return reportingService.generateStatementOfActivity(
                departmentId,
                activityFrom.getValue(),
                activityTo.getValue(),
                format
        );
    }
}