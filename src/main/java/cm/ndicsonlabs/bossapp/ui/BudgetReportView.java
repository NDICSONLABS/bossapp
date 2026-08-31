// src/main/java/com/institution/finance/ui/BudgetReportView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.BudgetLineRow;
import cm.ndicsonlabs.bossapp.dto.FundUtilizationLine;
import cm.ndicsonlabs.bossapp.dto.GrantUtilizationLine;
import cm.ndicsonlabs.bossapp.service.BudgetReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "budget-reports", layout = MainLayout.class)
@PermitAll
public class BudgetReportView extends VerticalLayout {

    private final Grid<BudgetLineRow> lineGrid = new Grid<>(BudgetLineRow.class);
    private final Grid<FundUtilizationLine> fundGrid = new Grid<>(FundUtilizationLine.class);
    private final Grid<GrantUtilizationLine> grantGrid = new Grid<>(GrantUtilizationLine.class);

    public BudgetReportView(BudgetReportService reportService) {
        lineGrid.setColumns(
                "fundCode",
                "grantCode",
                "departmentName",
                "expenseCategory",
                "originalAmount",
                "adjustedAmount",
                "reservedAmount",
                "spentAmount",
                "availableAmount",
                "budgetStatus"
        );

        fundGrid.setColumns(
                "fundCode",
                "fundName",
                "budgetAmount",
                "spentAmount",
                "availableAmount"
        );

        grantGrid.setColumns(
                "grantCode",
                "grantName",
                "donorName",
                "awardAmount",
                "allocatedBudget",
                "spentAmount",
                "remainingBudget"
        );

        Button refreshButton = new Button("Refresh", e -> {
            lineGrid.setItems(reportService.budgetLines());
            fundGrid.setItems(reportService.fundUtilization());
            grantGrid.setItems(reportService.grantUtilization());
        });

        lineGrid.setItems(reportService.budgetLines());
        fundGrid.setItems(reportService.fundUtilization());
        grantGrid.setItems(reportService.grantUtilization());

        add(
                new H2("Budget Reports"),
                new HorizontalLayout(refreshButton),

                new H2("Budget Lines"),
                lineGrid,

                new H2("Fund Utilization"),
                fundGrid,

                new H2("Grant Utilization"),
                grantGrid
        );
    }
}