// src/main/java/com/institution/finance/ui/SupplierInvoiceView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
import cm.ndicsonlabs.bossapp.service.FinanceService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

@Route(value = "supplier-invoices", layout = MainLayout.class)
@PermitAll
public class SupplierInvoiceView extends VerticalLayout {

    private final Grid<SupplierInvoice> grid = new Grid<>(SupplierInvoice.class);

    public SupplierInvoiceView(
            SupplierInvoiceRepository invoiceRepository,
            SupplierRepository supplierRepository,
            DepartmentRepository departmentRepository,
            FinanceService financeService
    ) {
        grid.setColumns("invoiceNumber", "invoiceDate", "dueDate", "totalAmount", "paidAmount", "status");
        grid.setItems(invoiceRepository.findAll());

        Button newInvoiceButton = new Button(
                "New Invoice",
                e -> openNewInvoiceDialog(invoiceRepository, supplierRepository, departmentRepository)
        );

        Button payButton = new Button(
                "Record Payment",
                e -> openPaymentDialog(invoiceRepository, financeService)
        );

        add(
                new H2("Supplier Invoices"),
                new HorizontalLayout(newInvoiceButton, payButton),
                grid
        );
    }

    private void openNewInvoiceDialog(
            SupplierInvoiceRepository invoiceRepository,
            SupplierRepository supplierRepository,
            DepartmentRepository departmentRepository
    ) {
        Dialog dialog = new Dialog();

        ComboBox<Supplier> supplierBox = new ComboBox<>("Supplier");
        supplierBox.setItems(supplierRepository.findAll());
        supplierBox.setItemLabelGenerator(Supplier::getName);

        ComboBox<Department> departmentBox = new ComboBox<>("Department");
        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);

        TextField invoiceNumber = new TextField("Invoice Number");
        DatePicker invoiceDate = new DatePicker("Invoice Date");
        DatePicker dueDate = new DatePicker("Due Date");
        BigDecimalField totalAmount = new BigDecimalField("Total Amount");

        Button save = new Button("Save", event -> {
            if (supplierBox.getValue() == null || departmentBox.getValue() == null ||
                    invoiceNumber.isEmpty() || totalAmount.getValue() == null ||
                    totalAmount.getValue().signum() <= 0) {
                Notification.show("Supplier, department, invoice number, and positive amount are required.");
                return;
            }

            SupplierInvoice invoice = new SupplierInvoice();
            invoice.setSupplier(supplierBox.getValue());
            invoice.setDepartment(departmentBox.getValue());
            invoice.setInvoiceNumber(invoiceNumber.getValue());
            invoice.setInvoiceDate(invoiceDate.getValue() != null ? invoiceDate.getValue() : LocalDate.now());
            invoice.setDueDate(dueDate.getValue() != null ? dueDate.getValue() : invoice.getInvoiceDate());
            invoice.setTotalAmount(totalAmount.getValue());
            invoice.setPaidAmount(BigDecimal.ZERO);
            invoice.setStatus("POSTED");

            invoiceRepository.save(invoice);
            grid.setItems(invoiceRepository.findAll());
            dialog.close();
        });

        FormLayout form = new FormLayout(
                supplierBox,
                departmentBox,
                invoiceNumber,
                invoiceDate,
                dueDate,
                totalAmount
        );

        dialog.add(form, save);
        dialog.open();
    }

    private void openPaymentDialog(
            SupplierInvoiceRepository invoiceRepository,
            FinanceService financeService
    ) {
        SupplierInvoice selected = grid.asSingleSelect().getValue();

        if (selected == null) {
            Notification.show("Select a supplier invoice first.");
            return;
        }

        Dialog dialog = new Dialog();

        BigDecimalField amount = new BigDecimalField("Amount");
        TextField payee = new TextField("Payee");

        Button save = new Button("Save", event -> {
            try {
                financeService.recordSupplierPayment(selected, amount.getValue(), payee.getValue(), LocalDate.now());
                grid.setItems(invoiceRepository.findAll());
                dialog.close();
                Notification.show("Payment recorded.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        FormLayout form = new FormLayout(amount, payee);
        dialog.add(form, save);
        dialog.open();
    }
}