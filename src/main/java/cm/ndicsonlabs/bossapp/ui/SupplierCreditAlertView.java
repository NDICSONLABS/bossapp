package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.SupplierCreditAlert;
import cm.ndicsonlabs.bossapp.repository.SupplierCreditAlertRepository;
import cm.ndicsonlabs.bossapp.service.SupplierCreditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "supplier-credit-alerts", layout = MainLayout.class)
@PermitAll
public class SupplierCreditAlertView extends VerticalLayout {

    private final Grid<SupplierCreditAlert> grid = new Grid<>(SupplierCreditAlert.class);

    public SupplierCreditAlertView(
            SupplierCreditAlertRepository alertRepository,
            SupplierCreditService creditService
    ) {
        grid.addColumn(alert -> alert.getSupplier().getName()).setHeader("Supplier");
        grid.setColumns(
                "createdAt",
                "alertType",
                "severity",
                "dueDate",
                "amount",
                "acknowledged",
                "message"
        );
        grid.setItems(alertRepository.findTop500ByOrderByCreatedAtDesc());

        Button generateButton = new Button("Generate Alerts", e -> {
            int count = creditService.generateAlerts();
            grid.setItems(alertRepository.findTop500ByOrderByCreatedAtDesc());
            Notification.show("Generated " + count + " alert(s).");
        });

        Button acknowledgeButton = new Button("Acknowledge Selected Alert", e -> {
            SupplierCreditAlert selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select an alert.");
                return;
            }

            try {
                creditService.acknowledgeAlert(selected.getId());
                grid.setItems(alertRepository.findTop500ByOrderByCreatedAtDesc());
                Notification.show("Alert acknowledged.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(alertRepository.findTop500ByOrderByCreatedAtDesc())
        );

        add(
                new H2("Supplier Credit Alerts"),
                new HorizontalLayout(generateButton, acknowledgeButton, refreshButton),
                grid
        );
    }
}
