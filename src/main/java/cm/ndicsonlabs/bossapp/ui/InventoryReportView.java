// src/main/java/com/institution/finance/ui/InventoryReportView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.InventoryBalanceLine;
import cm.ndicsonlabs.bossapp.service.InventoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "inventory-reports", layout = MainLayout.class)
@PermitAll
public class InventoryReportView extends VerticalLayout {

    private final Grid<InventoryBalanceLine> balanceGrid = new Grid<>(InventoryBalanceLine.class);
    private final Grid<InventoryBalanceLine> expiryGrid = new Grid<>(InventoryBalanceLine.class);

    public InventoryReportView(InventoryService inventoryService) {
        balanceGrid.setColumns(
                "itemCode",
                "itemName",
                "locationCode",
                "batchNumber",
                "expiryDate",
                "quantityOnHand",
                "averageCost",
                "stockValue"
        );

        expiryGrid.setColumns(
                "itemCode",
                "itemName",
                "locationCode",
                "batchNumber",
                "expiryDate",
                "quantityOnHand",
                "averageCost",
                "stockValue"
        );

        Span valuation = new Span("Total stock value: " + inventoryService.valuation(null));

        IntegerField days = new IntegerField("Expiry Within Days");
        days.setValue(30);

        Button refreshButton = new Button("Refresh", e -> {
            balanceGrid.setItems(inventoryService.balanceLines(null));
            expiryGrid.setItems(inventoryService.expiringBalances(days.getValue() != null ? days.getValue() : 30));
            valuation.setText("Total stock value: " + inventoryService.valuation(null));
        });

        balanceGrid.setItems(inventoryService.balanceLines(null));
        expiryGrid.setItems(inventoryService.expiringBalances(30));

        add(
                new H2("Inventory Valuation and Balances"),
                valuation,
                new HorizontalLayout(days, refreshButton),
                balanceGrid,
                new H2("Expiring Stock"),
                expiryGrid
        );
    }
}