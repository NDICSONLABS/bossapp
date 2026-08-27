// src/main/java/com/institution/finance/ui/PurchaseOrderView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrderLine;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderLineRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
import cm.ndicsonlabs.bossapp.service.ProcurementService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;
import java.util.List;

@Route(value = "purchase-orders", layout = MainLayout.class)
@PermitAll
public class PurchaseOrderView extends VerticalLayout {

    private final Grid<PurchaseOrder> orderGrid = new Grid<>(PurchaseOrder.class);
    private final Grid<PurchaseOrderLine> lineGrid = new Grid<>(PurchaseOrderLine.class);

    public PurchaseOrderView(
            PurchaseOrderRepository orderRepository,
            PurchaseOrderLineRepository lineRepository,
            SupplierRepository supplierRepository,
            DepartmentRepository departmentRepository,
            ProcurementService procurementService
    ) {
        orderGrid.addColumn(order -> order.getSupplier().getName()).setHeader("Supplier");
        orderGrid.addColumn(order -> order.getDepartment().getName()).setHeader("Department");
        orderGrid.setColumns(
                "poNumber",
                "orderDate",
                "expectedDeliveryDate",
                "status",
                "totalAmount"
        );
        orderGrid.setItems(orderRepository.findByOrderByCreatedAtDesc());

        lineGrid.setColumns(
                "description",
                "quantity",
                "unitPrice",
                "taxPercent",
                "lineTotal",
                "receivedQuantity",
                "acceptedQuantity"
        );

        orderGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
            } else {
                lineGrid.setItems(lineRepository.findByPurchaseOrderId(event.getValue().getId()));
            }
        });

        Button newOrderButton = new Button("New Purchase Order", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Supplier> supplierBox = new ComboBox<>("Supplier");
            supplierBox.setItems(supplierRepository.findAll());
            supplierBox.setItemLabelGenerator(Supplier::getName);

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            DatePicker expectedDeliveryDate = new DatePicker("Expected Delivery Date");
            TextField currency = new TextField("Currency");

            TextField description = new TextField("Line Description");
            BigDecimalField quantity = new BigDecimalField("Quantity");
            BigDecimalField unitPrice = new BigDecimalField("Unit Price");
            BigDecimalField taxPercent = new BigDecimalField("Tax %");

            Button save = new Button("Save", event -> {
                try {
                    if (supplierBox.getValue() == null || departmentBox.getValue() == null) {
                        Notification.show("Supplier and department are required.");
                        return;
                    }

                    ProcurementService.NewPurchaseOrderLine line = new ProcurementService.NewPurchaseOrderLine(
                            description.getValue(),
                            quantity.getValue(),
                            unitPrice.getValue(),
                            taxPercent.getValue()
                    );

                    procurementService.createPurchaseOrder(
                            supplierBox.getValue(),
                            departmentBox.getValue(),
                            expectedDeliveryDate.getValue(),
                            currency.getValue(),
                            List.of(line)
                    );

                    orderGrid.setItems(orderRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Purchase order created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    supplierBox,
                    departmentBox,
                    expectedDeliveryDate,
                    currency,
                    description,
                    quantity,
                    unitPrice,
                    taxPercent
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button receiveButton = new Button("Receive Remaining Quantities", e -> {
            PurchaseOrder selected = orderGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a purchase order.");
                return;
            }

            try {
                procurementService.receiveRemainingQuantities(selected.getId(), LocalDate.now());
                orderGrid.setItems(orderRepository.findByOrderByCreatedAtDesc());
                lineGrid.setItems(lineRepository.findByPurchaseOrderId(selected.getId()));
                Notification.show("Goods receipt created.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button invoiceButton = new Button("Create Invoice from PO", e -> {
            PurchaseOrder selected = orderGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a purchase order.");
                return;
            }

            Dialog dialog = new Dialog();

            TextField invoiceNumber = new TextField("Invoice Number");
            DatePicker invoiceDate = new DatePicker("Invoice Date");
            DatePicker dueDate = new DatePicker("Due Date");
            BigDecimalField totalAmount = new BigDecimalField("Invoice Amount");

            invoiceDate.setValue(LocalDate.now());
            totalAmount.setValue(selected.getTotalAmount());

            Button save = new Button("Create Invoice", event -> {
                try {
                    procurementService.createInvoiceFromPurchaseOrder(
                            selected.getId(),
                            invoiceNumber.getValue(),
                            invoiceDate.getValue(),
                            dueDate.getValue(),
                            totalAmount.getValue()
                    );

                    dialog.close();
                    Notification.show("Supplier invoice created and matching executed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(invoiceNumber, invoiceDate, dueDate, totalAmount);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                orderGrid.setItems(orderRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Purchase Orders"),
                new HorizontalLayout(newOrderButton, receiveButton, invoiceButton, refreshButton),
                orderGrid,
                new H2("Purchase Order Lines"),
                lineGrid
        );
    }
}