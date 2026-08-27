// src/main/java/com/institution/finance/ui/GlReconciliationView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.GlReconciliation;
import cm.ndicsonlabs.bossapp.repository.GlReconciliationRepository;
import cm.ndicsonlabs.bossapp.service.GlReconciliationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "gl-reconciliation", layout = MainLayout.class)
@PermitAll
public class GlReconciliationView extends VerticalLayout {

    private final Grid<GlReconciliation> grid = new Grid<>(GlReconciliation.class);

    public GlReconciliationView(
            GlReconciliationService reconciliationService,
            GlReconciliationRepository reconciliationRepository
    ) {
        DatePicker asOf = new DatePicker("As Of");
        asOf.setValue(LocalDate.now());

        Button runButton = new Button("Run Reconciliation", e -> {
            try {
                reconciliationService.runReconciliation(asOf.getValue());
                grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());
                Notification.show("Reconciliation completed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc())
        );

        grid.setColumns(
                "reconciliationDate",
                "sourceType",
                "subledgerAmount",
                "glAmount",
                "variance",
                "status",
                "notes"
        );

        grid.setItems(reconciliationRepository.findTop500ByOrderByCreatedAtDesc());

        add(
                new H2("Sub-Ledger to GL Reconciliation"),
                new HorizontalLayout(asOf, runButton, refreshButton),
                grid
        );
    }
}