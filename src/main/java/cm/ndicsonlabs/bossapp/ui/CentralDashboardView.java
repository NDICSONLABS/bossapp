// src/main/java/com/institution/finance/ui/CentralDashboardView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.DepartmentSubmission;
import cm.ndicsonlabs.bossapp.repository.DepartmentSubmissionRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "central-accounting", layout = MainLayout.class)
@PermitAll
public class CentralDashboardView extends VerticalLayout {

    public CentralDashboardView(DepartmentSubmissionRepository submissionRepository) {
        Grid<DepartmentSubmission> grid = new Grid<>(DepartmentSubmission.class);

        grid.addColumn(submission -> submission.getDepartment().getName()).setHeader("Department");
        grid.addColumn(submission -> submission.getPeriod().toString()).setHeader("Period");
        grid.setColumns(
                "status",
                "transactionCount",
                "closingApBalance",
                "closingArBalance",
                "submittedBy",
                "centralReviewedBy"
        );

        grid.setItems(submissionRepository.findByOrderByCreatedAtDesc());

        add(
                new H2("Central Accounting Dashboard"),
                new Span("Total submissions: " + submissionRepository.count()),
                new Span("Draft submissions: " + submissionRepository.countByStatus("DRAFT")),
                new Span("Department approved: " + submissionRepository.countByStatus("DEPARTMENT_APPROVED")),
                new Span("Submitted: " + submissionRepository.countByStatus("SUBMITTED")),
                new Span("Under central review: " + submissionRepository.countByStatus("UNDER_CENTRAL_REVIEW")),
                new Span("Accepted: " + submissionRepository.countByStatus("ACCEPTED")),
                new Span("Rejected: " + submissionRepository.countByStatus("REJECTED")),
                grid
        );
    }
}