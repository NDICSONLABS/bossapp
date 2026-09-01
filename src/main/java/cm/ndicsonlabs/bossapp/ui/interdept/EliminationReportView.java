// src/main/java/com/institution/finance/ui/EliminationReportView.java
package cm.ndicsonlabs.bossapp.ui.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import cm.ndicsonlabs.bossapp.service.interdept.EliminationReportService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "elimination-report", layout = MainLayout.class)
@PermitAll
public class EliminationReportView extends VerticalLayout {

    private final Grid<InternalInvoice> openInvoiceGrid = new Grid<>(InternalInvoice.class);

    public EliminationReportView(EliminationReportService reportService) {
        DatePicker from = new DatePicker("From");
        from.setValue(LocalDate.now().withDayOfYear(1));

        DatePicker to = new DatePicker("To");
        to.setValue(LocalDate.now());

        Span internalRevenue = new Span();
        Span allocations = new Span();
        Span openReceivables = new Span();

        Button refreshButton = new Button("Refresh", e -> {
            internalRevenue.setText("Internal Revenue to Eliminate: " + reportService.internalRevenue(from.getValue(), to.getValue()));
            allocations.setText("Cost Allocations: " + reportService.allocations(from.getValue(), to.getValue()));
            openReceivables.setText("Open Internal Receivables/Payables: " + reportService.openInternalReceivables());

            openInvoiceGrid.setItems(reportService.openInvoices());
        });

        openInvoiceGrid.addColumn(invoice -> invoice.getProviderDepartment().getName()).setHeader("Provider");
        openInvoiceGrid.addColumn(invoice -> invoice.getReceiverDepartment().getName()).setHeader("Receiver");
        openInvoiceGrid.setColumns("invoiceNumber", "transactionDate", "amount", "status");

        refreshButton.click();

        add(
                new H2("Consolidation Elimination Report"),
                new HorizontalLayout(from, to, refreshButton),
                internalRevenue,
                allocations,
                openReceivables,
                new H2("Open Internal Invoices"),
                openInvoiceGrid
        );
    }
}