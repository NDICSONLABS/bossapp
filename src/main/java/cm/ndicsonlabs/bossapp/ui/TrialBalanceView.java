// src/main/java/com/institution/finance/ui/TrialBalanceView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.dto.TrialBalanceLine;
import cm.ndicsonlabs.bossapp.service.AccountingReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "trial-balance", layout = MainLayout.class)
@PermitAll
public class TrialBalanceView extends VerticalLayout {

    private final Grid<TrialBalanceLine> grid = new Grid<>(TrialBalanceLine.class);

    public TrialBalanceView(AccountingReportService reportService) {
        DatePicker from = new DatePicker("From");
        DatePicker to = new DatePicker("To");

        from.setValue(LocalDate.now().withDayOfYear(1));
        to.setValue(LocalDate.now());

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(reportService.trialBalance(from.getValue(), to.getValue()))
        );

        grid.setColumns(
                "accountCode",
                "accountName",
                "accountType",
                "totalDebit",
                "totalCredit",
                "netBalance"
        );

        grid.setItems(reportService.trialBalance(from.getValue(), to.getValue()));

        add(
                new H2("Trial Balance"),
                new HorizontalLayout(from, to, refreshButton),
                grid
        );
    }
}