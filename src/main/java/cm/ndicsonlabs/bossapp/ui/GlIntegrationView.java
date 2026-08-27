// src/main/java/com/institution/finance/ui/GlIntegrationView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.GlIntegrationLog;
import cm.ndicsonlabs.bossapp.repository.GlIntegrationLogRepository;
import cm.ndicsonlabs.bossapp.service.GlIntegrationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "gl-integration", layout = MainLayout.class)
@PermitAll
public class GlIntegrationView extends VerticalLayout {

    private final Grid<GlIntegrationLog> grid = new Grid<>(GlIntegrationLog.class);

    public GlIntegrationView(
            GlIntegrationService glIntegrationService,
            GlIntegrationLogRepository logRepository
    ) {
        grid.setColumns(
                "createdAt",
                "sourceType",
                "sourceId",
                "action",
                "status",
                "message"
        );

        grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc());

        Button postStudentCharges = new Button("Post Student Charges", e -> {
            int count = glIntegrationService.postAllStudentCharges();
            grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc());
            Notification.show("Processed " + count + " student charge(s).");
        });

        Button postPatientCharges = new Button("Post Patient Charges", e -> {
            int count = glIntegrationService.postAllPatientCharges();
            grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc());
            Notification.show("Processed " + count + " patient charge(s).");
        });

        Button postSupplierInvoices = new Button("Post Supplier Invoices", e -> {
            int count = glIntegrationService.postAllSupplierInvoices();
            grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc());
            Notification.show("Processed " + count + " supplier invoice(s).");
        });

        Button postPayments = new Button("Post Payments", e -> {
            int count = glIntegrationService.postAllPayments();
            grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc());
            Notification.show("Processed " + count + " payment(s).");
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(logRepository.findTop500ByOrderByCreatedAtDesc())
        );

        add(
                new H2("GL Integration"),
                new HorizontalLayout(
                        postStudentCharges,
                        postPatientCharges,
                        postSupplierInvoices,
                        postPayments,
                        refreshButton
                ),
                grid
        );
    }
}