// src/main/java/com/institution/finance/ui/DepartmentSubmissionView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.DepartmentSubmission;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentSubmissionRepository;
import cm.ndicsonlabs.bossapp.service.CentralAccountingService;
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

@Route(value = "department-submissions", layout = MainLayout.class)
@PermitAll
public class DepartmentSubmissionView extends VerticalLayout {

    private final Grid<DepartmentSubmission> grid = new Grid<>(DepartmentSubmission.class);

    private final ComboBox<Department> departmentBox = new ComboBox<>("Department");
    private final ComboBox<AccountingPeriod> periodBox = new ComboBox<>("Accounting Period");

    public DepartmentSubmissionView(
            DepartmentRepository departmentRepository,
            AccountingPeriodRepository periodRepository,
            DepartmentSubmissionRepository submissionRepository,
            CentralAccountingService centralAccountingService
    ) {
        departmentBox.setItems(departmentRepository.findAll());
        departmentBox.setItemLabelGenerator(Department::getName);

        periodBox.setItems(periodRepository.findByOrderByFiscalYearDescPeriodNumberAsc());
        periodBox.setItemLabelGenerator(period -> period.getFiscalYear() + " P" + period.getPeriodNumber());

        grid.addColumn(submission -> submission.getDepartment().getName()).setHeader("Department");
        grid.addColumn(submission -> submission.getPeriod().toString()).setHeader("Period");
        grid.setColumns(
                "status",
                "openingApBalance",
                "newApAmount",
                "apPayments",
                "closingApBalance",
                "openingArBalance",
                "newArAmount",
                "arCollections",
                "closingArBalance",
                "transactionCount"
        );

        grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());

        Button prepareButton = new Button("Prepare Draft", event -> {
            try {
                if (departmentBox.getValue() == null || periodBox.getValue() == null) {
                    Notification.show("Select department and accounting period.");
                    return;
                }

                centralAccountingService.prepareDraft(
                        departmentBox.getValue().getId(),
                        periodBox.getValue().getId()
                );

                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Draft submission prepared.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button approveButton = new Button("Approve by Department", event -> {
            try {
                DepartmentSubmission selected = grid.asSingleSelect().getValue();

                if (selected == null) {
                    Notification.show("Select a submission.");
                    return;
                }

                centralAccountingService.approveByDepartment(selected.getId());
                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Submission approved by department.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button submitButton = new Button("Submit to Central", event -> {
            try {
                DepartmentSubmission selected = grid.asSingleSelect().getValue();

                if (selected == null) {
                    Notification.show("Select a submission.");
                    return;
                }

                centralAccountingService.submitToCentral(selected.getId());
                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Submission sent to central accounting.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button reviewButton = new Button("Start Central Review", event -> {
            try {
                DepartmentSubmission selected = grid.asSingleSelect().getValue();

                if (selected == null) {
                    Notification.show("Select a submission.");
                    return;
                }

                centralAccountingService.startCentralReview(selected.getId());
                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Submission moved under central review.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button acceptButton = new Button("Accept", event -> {
            try {
                DepartmentSubmission selected = grid.asSingleSelect().getValue();

                if (selected == null) {
                    Notification.show("Select a submission.");
                    return;
                }

                centralAccountingService.accept(selected.getId());
                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                Notification.show("Submission accepted.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button rejectButton = new Button("Reject", event -> {
            DepartmentSubmission selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a submission.");
                return;
            }

            Dialog dialog = new Dialog();
            TextArea comments = new TextArea("Rejection Comments");
            comments.setWidthFull();

            Button save = new Button("Reject Submission", confirm -> {
                try {
                    centralAccountingService.reject(selected.getId(), comments.getValue());
                    grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Submission rejected.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(comments);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", event ->
                grid.setItems(submissionRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Department Submissions"),
                new HorizontalLayout(departmentBox, periodBox),
                new HorizontalLayout(
                        prepareButton,
                        approveButton,
                        submitButton,
                        reviewButton,
                        acceptButton,
                        rejectButton,
                        refreshButton
                ),
                grid
        );
    }
}