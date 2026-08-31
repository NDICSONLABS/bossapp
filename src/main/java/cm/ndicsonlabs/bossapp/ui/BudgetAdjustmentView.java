// src/main/java/com/institution/finance/ui/BudgetAdjustmentView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.service.BudgetControlService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "budget-adjustments", layout = MainLayout.class)
@PermitAll
public class BudgetAdjustmentView extends VerticalLayout {

    private final Grid<BudgetLine> grid = new Grid<>(BudgetLine.class);

    public BudgetAdjustmentView(
            BudgetLineRepository lineRepository,
            BudgetControlService budgetControlService
    ) {
        grid.addColumn(line -> line.getBudgetHeader().getFund().getCode()).setHeader("Fund");
        grid.addColumn(line -> line.getBudgetHeader().getDepartment().getName()).setHeader("Department");
        grid.addColumn(line -> budgetControlService.availableAmount(line)).setHeader("Available");
        grid.setColumns(
                "expenseCategory",
                "description",
                "originalAmount",
                "adjustedAmount",
                "reservedAmount",
                "spentAmount"
        );
        grid.setItems(lineRepository.findAll());

        Button adjustButton = new Button("Adjust Selected Budget Line", e -> {
            BudgetLine selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a budget line.");
                return;
            }

            Dialog dialog = new Dialog();

            BigDecimalField amount = new BigDecimalField("Adjustment Amount");
            TextArea reason = new TextArea("Reason");

            Button save = new Button("Save Adjustment", event -> {
                try {
                    budgetControlService.adjustLine(
                            selected.getId(),
                            amount.getValue(),
                            reason.getValue()
                    );

                    grid.setItems(lineRepository.findAll());
                    dialog.close();
                    Notification.show("Budget adjustment saved.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(amount, reason);
            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e -> grid.setItems(lineRepository.findAll()));

        add(
                new H2("Budget Adjustments"),
                new HorizontalLayout(adjustButton, refreshButton),
                grid
        );
    }
}