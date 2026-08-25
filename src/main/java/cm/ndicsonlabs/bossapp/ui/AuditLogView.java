// src/main/java/com/institution/finance/ui/AuditLogView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AuditLog;
import cm.ndicsonlabs.bossapp.repository.AuditLogRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "audit-log", layout = MainLayout.class)
@PermitAll
public class AuditLogView extends VerticalLayout {

    public AuditLogView(AuditLogRepository auditLogRepository) {
        Grid<AuditLog> grid = new Grid<>(AuditLog.class);

        grid.setColumns(
                "createdAt",
                "username",
                "entityType",
                "entityId",
                "action",
                "reason"
        );

        grid.setItems(auditLogRepository.findTop500ByOrderByCreatedAtDesc());

        add(new H2("Audit Log"), grid);
    }
}