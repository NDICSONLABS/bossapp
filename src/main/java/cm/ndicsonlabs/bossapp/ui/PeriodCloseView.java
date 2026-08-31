// src/main/java/com/institution/finance/ui/PeriodCloseView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.PeriodCloseTask;
import cm.ndicsonlabs.bossapp.domain.PeriodCloseValidation;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.PeriodCloseTaskRepository;
import cm.ndicsonlabs.bossapp.repository.PeriodCloseValidationRepository;
import cm.ndicsonlabs.bossapp.service.PeriodCloseService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "period-close", layout = MainLayout.class)
@PermitAll
public class PeriodCloseView extends VerticalLayout {

    private final Grid<PeriodCloseTask> taskGrid = new Grid<>(PeriodCloseTask.class);
    private final Grid<PeriodCloseValidation> validationGrid = new Grid<>(PeriodCloseValidation.class);

    public PeriodCloseView(
            AccountingPeriodRepository periodRepository,
            PeriodCloseTaskRepository taskRepository,
            PeriodCloseValidationRepository validationRepository,
            PeriodCloseService periodCloseService
    ) {
        ComboBox<AccountingPeriod> periodBox = new ComboBox<>("Accounting Period");
        periodBox.setItems(periodRepository.findByOrderByFiscalYearDescPeriodNumberAsc());
        periodBox.setItemLabelGenerator(period -> period.getFiscalYear() + " P" + period.getPeriodNumber());

        taskGrid.setColumns(
                "taskCode",
                "description",
                "required",
                "status",
                "completedBy",
                "completedAt",
                "notes"
        );

        validationGrid.setColumns(
                "validationCode",
                "status",
                "message",
                "createdAt"
        );

        periodBox.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                taskGrid.setItems(List.of());
                validationGrid.setItems(List.of());
            } else {
                taskGrid.setItems(taskRepository.findByPeriodIdOrderByCreatedAtAsc(event.getValue().getId()));
                validationGrid.setItems(validationRepository.findByPeriodIdOrderByCreatedAtDesc(event.getValue().getId()));
            }
        });

        Button generateChecklistButton = new Button("Generate Checklist", e -> {
            if (periodBox.getValue() == null) {
                Notification.show("Select a period.");
                return;
            }

            periodCloseService.generateChecklist(periodBox.getValue().getId());
            taskGrid.setItems(taskRepository.findByPeriodIdOrderByCreatedAtAsc(periodBox.getValue().getId()));
            Notification.show("Period close checklist generated.");
        });

        Button runValidationsButton = new Button("Run Validations", e -> {
            if (periodBox.getValue() == null) {
                Notification.show("Select a period.");
                return;
            }

            periodCloseService.runValidations(periodBox.getValue().getId());
            validationGrid.setItems(validationRepository.findByPeriodIdOrderByCreatedAtDesc(periodBox.getValue().getId()));
            Notification.show("Period close validations completed.");
        });

        Button completeTaskButton = new Button("Complete Selected Task", e -> {
            PeriodCloseTask selected = taskGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a task.");
                return;
            }

            Dialog dialog = new Dialog();
            TextArea notes = new TextArea("Notes");

            Button confirm = new Button("Complete Task", event -> {
                try {
                    periodCloseService.completeTask(
                            selected.getPeriod().getId(),
                            selected.getTaskCode(),
                            notes.getValue()
                    );

                    taskGrid.setItems(taskRepository.findByPeriodIdOrderByCreatedAtAsc(selected.getPeriod().getId()));
                    dialog.close();
                    Notification.show("Task completed.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(notes);
            dialog.add(form, confirm);
            dialog.open();
        });

        Button softCloseButton = new Button("Soft Close", e -> {
            if (periodBox.getValue() == null) {
                Notification.show("Select a period.");
                return;
            }

            try {
                periodCloseService.softClose(periodBox.getValue().getId());
                Notification.show("Period soft closed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button closeButton = new Button("Final Close", e -> {
            if (periodBox.getValue() == null) {
                Notification.show("Select a period.");
                return;
            }

            try {
                periodCloseService.close(periodBox.getValue().getId());
                Notification.show("Period finally closed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button lockButton = new Button("Hard Lock", e -> {
            if (periodBox.getValue() == null) {
                Notification.show("Select a period.");
                return;
            }

            try {
                periodCloseService.lock(periodBox.getValue().getId());
                Notification.show("Period hard locked.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        add(
                new H2("Period Close"),
                new HorizontalLayout(
                        periodBox,
                        generateChecklistButton,
                        runValidationsButton,
                        completeTaskButton,
                        softCloseButton,
                        closeButton,
                        lockButton
                ),
                new H2("Close Tasks"),
                taskGrid,
                new H2("Close Validations"),
                validationGrid
        );
    }
}