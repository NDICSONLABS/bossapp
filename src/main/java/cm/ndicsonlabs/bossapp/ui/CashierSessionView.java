// src/main/java/com/institution/finance/ui/CashierSessionView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.CashierSession;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.repository.CashierSessionRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.service.CashierService;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "cashier-sessions", layout = MainLayout.class)
@PermitAll
public class CashierSessionView extends VerticalLayout {

    private final Grid<CashierSession> grid = new Grid<>(CashierSession.class);

    public CashierSessionView(
            CashierSessionRepository sessionRepository,
            DepartmentRepository departmentRepository,
            CashierService cashierService
    ) {
        grid.addColumn(session -> session.getDepartment().getName()).setHeader("Department");
        grid.setColumns(
                "sessionDate",
                "cashierUsername",
                "openingBalance",
                "expectedClosingBalance",
                "actualClosingBalance",
                "variance",
                "status",
                "approvedBy"
        );
        grid.setItems(sessionRepository.findByOrderByCreatedAtDesc());

        ComboBox<Department> departmentBox = new ComboBox<>("Department");
        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);

        DatePicker sessionDate = new DatePicker("Session Date");
        sessionDate.setValue(LocalDate.now());

        BigDecimalField openingBalance = new BigDecimalField("Opening Balance");

        Button openButton = new Button("Open Session", e -> {
            try {
                if (departmentBox.getValue() == null) {
                    Notification.show("Select department.");
                    return;
                }

                cashierService.openSession(
                        departmentBox.getValue().getId(),
                        sessionDate.getValue(),
                        openingBalance.getValue()
                );

                grid.setItems(sessionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Cashier session opened.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button manualTransactionButton = new Button("Add Manual Transaction", e -> {
            CashierSession selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a cashier session.");
                return;
            }

            Dialog dialog = new Dialog();

            TextField method = new TextField("Payment Method");
            ComboBox<String> direction = new ComboBox<>("Direction");
            direction.setItems("IN", "OUT");
            direction.setValue("IN");

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField description = new TextField("Description");

            Button save = new Button("Save", event -> {
                try {
                    cashierService.addManualTransaction(
                            selected.getId(),
                            method.getValue(),
                            direction.getValue(),
                            amount.getValue(),
                            description.getValue()
                    );

                    dialog.close();
                    Notification.show("Manual transaction added.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(method, direction, amount, description);
            dialog.add(form, save);
            dialog.open();
        });

        Button closeButton = new Button("Close Session", e -> {
            CashierSession selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a cashier session.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField actualClosing = new BigDecimalField("Actual Closing Balance");
            TextArea explanation = new TextArea("Explanation");

            Button save = new Button("Close", event -> {
                try {
                    cashierService.closeSession(
                            selected.getId(),
                            actualClosing.getValue(),
                            explanation.getValue()
                    );

                    grid.setItems(sessionRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Cashier session closed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(actualClosing, explanation);
            dialog.add(form, save);
            dialog.open();
        });

        Button approveButton = new Button("Approve Session", e -> {
            CashierSession selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a cashier session.");
                return;
            }

            try {
                cashierService.approveSession(selected.getId());
                grid.setItems(sessionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Cashier session approved.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(sessionRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Daily Cashier Reconciliation"),
                new HorizontalLayout(departmentBox, sessionDate, openingBalance),
                new HorizontalLayout(openButton, manualTransactionButton, closeButton, approveButton, refreshButton),
                grid
        );
    }
}