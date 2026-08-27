package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.SupplierCreditSummary;
import cm.ndicsonlabs.bossapp.service.SupplierCreditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "supplier-credit-control", layout = MainLayout.class)
@PermitAll
public class SupplierCreditControlView extends VerticalLayout {

    private final Grid<SupplierCreditSummary> grid = new Grid<>(SupplierCreditSummary.class);

    public SupplierCreditControlView(SupplierCreditService creditService) {
        grid.setColumns(
                "supplierCode",
                "supplierName",
                "category",
                "subcategory",
                "creditLimit",
                "outstanding",
                "availableCredit",
                "utilizationPercent",
                "creditHold"
        );
        grid.setItems(creditService.creditSummaries());

        Button configureButton = new Button("Configure Credit Control", e -> {
            SupplierCreditSummary selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a supplier first.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField creditLimit = new BigDecimalField("Credit Limit");
            IntegerField termsDays = new IntegerField("Credit Terms Days");
            IntegerField alertThreshold = new IntegerField("Alert Threshold Days");
            Checkbox hold = new Checkbox("Hold on Limit Exceeded");

            creditLimit.setValue(selected.getCreditLimit());

            Button save = new Button("Save", event -> {
                try {
                    creditService.saveCreditControl(
                            selected.getSupplierId(),
                            creditLimit.getValue(),
                            termsDays.getValue(),
                            alertThreshold.getValue(),
                            hold.getValue()
                    );

                    grid.setItems(creditService.creditSummaries());
                    dialog.close();
                    Notification.show("Supplier credit control saved.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(creditLimit, termsDays, alertThreshold, hold);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(creditService.creditSummaries())
        );

        add(
                new H2("Supplier Credit Control"),
                new HorizontalLayout(configureButton, refreshButton),
                grid
        );
    }
}