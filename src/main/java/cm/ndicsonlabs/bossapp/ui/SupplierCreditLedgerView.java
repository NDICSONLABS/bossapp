package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.AgingLine;
import cm.ndicsonlabs.bossapp.service.SupplierCreditService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "supplier-credit-ledger", layout = MainLayout.class)
@PermitAll
public class SupplierCreditLedgerView extends VerticalLayout {

    private final Grid<AgingLine> grid = new Grid<>(AgingLine.class);

    public SupplierCreditLedgerView(SupplierCreditService creditService) {
        ComboBox<String> categoryFilter = new ComboBox<>("Supplier Category");
        categoryFilter.setItems("ALL", "DRUG", "MEDICAL_SUPPLY", "LABORATORY", "GENERAL");
        categoryFilter.setValue("ALL");

        Button refreshButton = new Button("Refresh", e -> {
            String filter = "ALL".equals(categoryFilter.getValue()) ? null : categoryFilter.getValue();
            grid.setItems(creditService.supplierCreditAging(filter));
        });

        grid.setColumns(
                "entityName",
                "reference",
                "dueDate",
                "originalAmount",
                "paidAmount",
                "outstandingAmount",
                "overdueDays",
                "agingBucket"
        );

        grid.setItems(creditService.supplierCreditAging(null));

        add(
                new H2("Drug and Medical Supply Supplier Credit Ledger"),
                new HorizontalLayout(categoryFilter, refreshButton),
                grid
        );
    }
}