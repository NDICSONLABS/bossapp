// src/main/java/com/institution/finance/ui/InternalInvoiceView.java
package cm.ndicsonlabs.bossapp.ui.interdept;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalServiceCatalog;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalInvoiceRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalServiceCatalogRepository;
import cm.ndicsonlabs.bossapp.service.interdept.InternalBillingService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
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
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "internal-invoices", layout = MainLayout.class)
@PermitAll
public class InternalInvoiceView extends VerticalLayout {

    private final Grid<InternalInvoice> grid = new Grid<>(InternalInvoice.class);

    public InternalInvoiceView(
            InternalInvoiceRepository invoiceRepository,
            DepartmentRepository departmentRepository,
            InternalServiceCatalogRepository serviceRepository,
            InternalBillingService billingService
    ) {
        grid.addColumn(invoice -> invoice.getProviderDepartment().getName()).setHeader("Provider");
        grid.addColumn(invoice -> invoice.getReceiverDepartment().getName()).setHeader("Receiver");
        grid.addColumn(invoice -> invoice.getService() != null ? invoice.getService().getName() : "").setHeader("Service");
        grid.setColumns(
                "invoiceNumber",
                "transactionDate",
                "dueDate",
                "amount",
                "status"
        );
        grid.setItems(invoiceRepository.findByOrderByCreatedAtDesc());

        Button newButton = new Button("New Internal Invoice", e -> {
            Dialog dialog = new Dialog();

            ComboBox<Department> providerBox = new ComboBox<>("Provider Department");
            providerBox.setItems(departmentRepository.findAll());
            providerBox.setItemLabelGenerator(Department::getName);

            ComboBox<Department> receiverBox = new ComboBox<>("Receiver Department");
            receiverBox.setItems(departmentRepository.findAll());
            receiverBox.setItemLabelGenerator(Department::getName);

            ComboBox<InternalServiceCatalog> serviceBox = new ComboBox<>("Service");
            serviceBox.setItems(serviceRepository.findByActiveTrueOrderByCode());
            serviceBox.setItemLabelGenerator(InternalServiceCatalog::toString);
            serviceBox.setClearButtonVisible(true);

            TextArea description = new TextArea("Description");
            BigDecimalField amount = new BigDecimalField("Amount");
            DatePicker transactionDate = new DatePicker("Transaction Date");
            DatePicker dueDate = new DatePicker("Due Date");

            transactionDate.setValue(LocalDate.now());

            Button save = new Button("Save Draft", event -> {
                try {
                    billingService.createInvoice(
                            providerBox.getValue().getId(),
                            receiverBox.getValue().getId(),
                            serviceBox.getValue() != null ? serviceBox.getValue().getId() : null,
                            description.getValue(),
                            amount.getValue(),
                            transactionDate.getValue(),
                            dueDate.getValue()
                    );

                    grid.setItems(invoiceRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Internal invoice created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    providerBox,
                    receiverBox,
                    serviceBox,
                    description,
                    amount,
                    transactionDate,
                    dueDate
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button postButton = new Button("Post Selected Invoice", e -> {
            InternalInvoice selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an internal invoice.");
                return;
            }

            try {
                billingService.postInvoice(selected.getId());
                grid.setItems(invoiceRepository.findByOrderByCreatedAtDesc());
                Notification.show("Internal invoice posted.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button settleButton = new Button("Settle Selected Invoice", e -> {
            InternalInvoice selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an internal invoice.");
                return;
            }

            try {
                billingService.settleInvoice(selected.getId());
                grid.setItems(invoiceRepository.findByOrderByCreatedAtDesc());
                Notification.show("Internal invoice settled.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(invoiceRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Internal Invoices"),
                new HorizontalLayout(newButton, postButton, settleButton, refreshButton),
                grid
        );
    }
}