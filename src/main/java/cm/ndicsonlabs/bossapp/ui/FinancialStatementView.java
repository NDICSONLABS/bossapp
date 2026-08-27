// src/main/java/com/institution/finance/ui/FinancialStatementView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.StatementLine;
import cm.ndicsonlabs.bossapp.service.FinancialStatementService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "financial-statements", layout = MainLayout.class)
@PermitAll
public class FinancialStatementView extends VerticalLayout {

    private final Grid<StatementLine> balanceGrid = new Grid<>(StatementLine.class);
    private final Grid<StatementLine> activityGrid = new Grid<>(StatementLine.class);

    public FinancialStatementView(FinancialStatementService statementService) {
        DatePicker balanceAsOf = new DatePicker("Balance Sheet As Of");
        balanceAsOf.setValue(LocalDate.now());

        Button balanceButton = new Button("Refresh Balance Sheet", e ->
                balanceGrid.setItems(statementService.statementOfFinancialPosition(balanceAsOf.getValue()))
        );

        DatePicker activityFrom = new DatePicker("Activity From");
        activityFrom.setValue(LocalDate.now().withDayOfYear(1));

        DatePicker activityTo = new DatePicker("Activity To");
        activityTo.setValue(LocalDate.now());

        Button activityButton = new Button("Refresh Activity Statement", e ->
                activityGrid.setItems(statementService.statementOfActivity(activityFrom.getValue(), activityTo.getValue()))
        );

        balanceGrid.setColumns("section", "line", "amount");
        activityGrid.setColumns("section", "line", "amount");

        balanceGrid.setItems(statementService.statementOfFinancialPosition(balanceAsOf.getValue()));
        activityGrid.setItems(statementService.statementOfActivity(activityFrom.getValue(), activityTo.getValue()));

        add(
                new H2("Financial Statements"),
                new HorizontalLayout(balanceAsOf, balanceButton),
                balanceGrid,
                new H2("Statement of Activity"),
                new HorizontalLayout(activityFrom, activityTo, activityButton),
                activityGrid
        );
    }
}