// src/main/java/com/institution/finance/ui/ProcurementInvoiceView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.ProcurementMatchIssue;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.ProcurementMatchIssueRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import cm.ndicsonlabs.bossapp.service.ProcurementService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "procurement-invoices", layout = MainLayout.class)
@PermitAll
public class ProcurementInvoiceView extends VerticalLayout {

    private final Grid<SupplierInvoice> grid = new Grid<>(SupplierInvoice.class);

    public ProcurementInvoiceView(
            SupplierInvoiceRepository invoiceRepository,
            ProcurementMatchIssueRepository issueRepository,
            ProcurementService procurementService
    ) {
        grid.addColumn(invoice -> invoice.getSupplier().getName()).setHeader("Supplier");
        grid.addColumn(invoice -> invoice.getPurchaseOrder() != null
                ? invoice.getPurchaseOrder().getPoNumber()
                : "").setHeader("Purchase Order");
        grid.setColumns(
                "invoiceNumber",
                "invoiceDate",
                "dueDate",
                "totalAmount",
                "paidAmount",
                "matchStatus"
        );
        grid.setItems(invoiceRepository.findAll());

        Button matchButton = new Button("Match Selected Invoice", e -> {
            SupplierInvoice selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a supplier invoice.");
                return;
            }

            try {
                procurementService.matchInvoice(selected.getId());
                grid.setItems(invoiceRepository.findAll());
                Notification.show("Matching completed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button issuesButton = new Button("Show Match Issues", e -> {
            SupplierInvoice selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a supplier invoice.");
                return;
            }

            List<ProcurementMatchIssue> issues = issueRepository.findBySupplierInvoiceId(selected.getId());

            Dialog dialog = new Dialog();
            TextArea text = new TextArea("Issues");
            text.setWidthFull();
            text.setReadOnly(true);

            StringBuilder builder = new StringBuilder();

            if (issues.isEmpty()) {
                builder.append("No match issues found.");
            } else {
                for (ProcurementMatchIssue issue : issues) {
                    builder.append(issue.getSeverity())
                            .append(" - ")
                            .append(issue.getIssueType())
                            .append(": ")
                            .append(issue.getMessage())
                            .append(System.lineSeparator());
                }
            }

            text.setValue(builder.toString());
            dialog.add(text);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(invoiceRepository.findAll())
        );

        add(
                new H2("Procurement Invoice Matching"),
                new HorizontalLayout(matchButton, issuesButton, refreshButton),
                grid
        );
    }
}