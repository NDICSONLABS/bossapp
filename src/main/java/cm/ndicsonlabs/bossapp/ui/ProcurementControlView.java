// src/main/java/com/institution/finance/ui/ProcurementControlView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.domain.SupplierStatementReconciliation;
import cm.ndicsonlabs.bossapp.dto.SupplierBalanceLine;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierStatementReconciliationRepository;
import cm.ndicsonlabs.bossapp.service.ProcurementControlService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "procurement-control", layout = MainLayout.class)
@PermitAll
public class ProcurementControlView extends VerticalLayout {

    private final Grid<SupplierBalanceLine> balanceGrid = new Grid<>(SupplierBalanceLine.class);
    private final Grid<PurchaseOrder> commitmentGrid = new Grid<>(PurchaseOrder.class);
    private final Grid<SupplierInvoice> forecastGrid = new Grid<>(SupplierInvoice.class);
    private final Grid<SupplierStatementReconciliation> reconciliationGrid = new Grid<>(SupplierStatementReconciliation.class);

    public ProcurementControlView(
            ProcurementControlService controlService,
            SupplierRepository supplierRepository,
            SupplierStatementReconciliationRepository reconciliationRepository
    ) {
        balanceGrid.setColumns(
                "supplierCode",
                "supplierName",
                "category",
                "totalInvoiced",
                "totalPaid",
                "outstanding",
                "overdue"
        );
        balanceGrid.setItems(controlService.supplierBalances());

        commitmentGrid.addColumn(order -> order.getSupplier().getName()).setHeader("Supplier");
        commitmentGrid.setColumns(
                "poNumber",
                "orderDate",
                "expectedDeliveryDate",
                "status",
                "totalAmount"
        );
        commitmentGrid.setItems(controlService.openCommitments());

        forecastGrid.addColumn(invoice -> invoice.getSupplier().getName()).setHeader("Supplier");
        forecastGrid.setColumns(
                "invoiceNumber",
                "dueDate",
                "totalAmount",
                "paidAmount",
                "status"
        );
        forecastGrid.setItems(controlService.paymentForecast(90));

        reconciliationGrid.addColumn(rec -> rec.getSupplier().getName()).setHeader("Supplier");
        reconciliationGrid.setColumns(
                "statementDate",
                "supplierBalance",
                "systemBalance",
                "variance",
                "status",
                "notes"
        );
        reconciliationGrid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());

        ComboBox<Supplier> supplierBox = new ComboBox<>("Supplier");
        supplierBox.setItems(supplierRepository.findAll());
        supplierBox.setItemLabelGenerator(Supplier::getName);

        DatePicker statementDate = new DatePicker("Statement Date");
        statementDate.setValue(LocalDate.now());

        BigDecimalField supplierBalance = new BigDecimalField("Supplier Statement Balance");

        TextArea notes = new TextArea("Notes");

        Button reconcileButton = new Button("Reconcile Supplier Statement", e -> {
            try {
                if (supplierBox.getValue() == null) {
                    Notification.show("Select a supplier.");
                    return;
                }

                controlService.reconcileSupplierStatement(
                        supplierBox.getValue().getId(),
                        statementDate.getValue(),
                        supplierBalance.getValue(),
                        notes.getValue()
                );

                reconciliationGrid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
                Notification.show("Supplier statement reconciliation completed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh All", e -> {
            balanceGrid.setItems(controlService.supplierBalances());
            commitmentGrid.setItems(controlService.openCommitments());
            forecastGrid.setItems(controlService.paymentForecast(90));
            reconciliationGrid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
        });

        add(
                new H2("Supplier Balances"),
                balanceGrid,

                new H2("Open Procurement Commitments"),
                commitmentGrid,

                new H2("Supplier Payment Forecast"),
                forecastGrid,

                new H2("Supplier Statement Reconciliation"),
                new HorizontalLayout(supplierBox, statementDate, supplierBalance, reconcileButton, refreshButton),
                notes,
                reconciliationGrid
        );
    }
}