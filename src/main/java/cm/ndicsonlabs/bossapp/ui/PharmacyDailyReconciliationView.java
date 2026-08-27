package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PharmacyDailyReconciliation;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PharmacyDailyReconciliationRepository;
import cm.ndicsonlabs.bossapp.service.SupplierCreditService;
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

@Route(value = "pharmacy-daily-reconciliation", layout = MainLayout.class)
@PermitAll
public class PharmacyDailyReconciliationView extends VerticalLayout {

    private final Grid<PharmacyDailyReconciliation> grid = new Grid<>(PharmacyDailyReconciliation.class);

    public PharmacyDailyReconciliationView(
            SupplierCreditService creditService,
            PharmacyDailyReconciliationRepository reconciliationRepository,
            DepartmentRepository departmentRepository
    ) {
        grid.addColumn(rec -> rec.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "reconciliationDate",
                "openingSupplierCredit",
                "newSupplierInvoices",
                "supplierPayments",
                "expectedClosingCredit",
                "actualClosingCredit",
                "variance",
                "status"
        );
        grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());

        ComboBox<Department> departmentBox = new ComboBox<>("Department");
        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);

        DatePicker date = new DatePicker("Date");
        date.setValue(LocalDate.now());

        Button openButton = new Button("Open Reconciliation", e -> {
            try {
                if (departmentBox.getValue() == null) {
                    Notification.show("Select a department.");
                    return;
                }

                creditService.openPharmacyReconciliation(
                        departmentBox.getValue().getId(),
                        date.getValue()
                );

                grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
                Notification.show("Pharmacy reconciliation opened.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button closeButton = new Button("Close Selected Reconciliation", e -> {
            PharmacyDailyReconciliation selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a reconciliation.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField actual = new BigDecimalField("Actual Closing Credit");
            TextArea explanation = new TextArea("Explanation");

            Button save = new Button("Close", event -> {
                try {
                    creditService.closePharmacyReconciliation(
                            selected.getId(),
                            actual.getValue(),
                            explanation.getValue()
                    );

                    grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Pharmacy reconciliation closed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(actual, explanation);
            dialog.add(form, save);
            dialog.open();
        });

        Button approveButton = new Button("Approve Selected Reconciliation", e -> {
            PharmacyDailyReconciliation selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a reconciliation.");
                return;
            }

            try {
                creditService.approvePharmacyReconciliation(selected.getId());
                grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
                Notification.show("Pharmacy reconciliation approved.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc())
        );

        add(
                new H2("Pharmacy Daily Reconciliation"),
                new HorizontalLayout(departmentBox, date, openButton, closeButton, approveButton, refreshButton),
                grid
        );
    }
}