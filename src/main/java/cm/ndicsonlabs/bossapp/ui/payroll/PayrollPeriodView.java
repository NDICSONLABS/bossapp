// src/main/java/com/institution/finance/ui/PayrollPeriodView.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollPeriod;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.repository.FundRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import cm.ndicsonlabs.bossapp.repository.payroll.PayrollPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.payroll.PayrollService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "payroll-periods", layout = MainLayout.class)
@PermitAll
public class PayrollPeriodView extends VerticalLayout {

    private final Grid<PayrollPeriod> grid = new Grid<>(PayrollPeriod.class);

    public PayrollPeriodView(
            PayrollPeriodRepository periodRepository,
            FundRepository fundRepository,
            GrantAwardRepository grantRepository,
            BudgetLineRepository budgetLineRepository,
            TreasuryAccountRepository treasuryAccountRepository,
            PayrollService payrollService
    ) {
        grid.addColumn(period -> period.getFund() != null ? period.getFund().getCode() : "").setHeader("Fund");
        grid.addColumn(period -> period.getGrantAward() != null ? period.getGrantAward().getCode() : "").setHeader("Grant");
        grid.setColumns(
                "fiscalYear",
                "periodNumber",
                "startDate",
                "endDate",
                "totalGross",
                "totalDeductions",
                "totalNet",
                "status",
                "paymentReference"
        );
        grid.setItems(periodRepository.findByOrderByCreatedAtDesc());

        Button newButton = new Button("New Payroll Period", e -> {
            Dialog dialog = new Dialog();

            IntegerField fiscalYear = new IntegerField("Fiscal Year");
            fiscalYear.setValue(LocalDate.now().getYear());

            IntegerField periodNumber = new IntegerField("Period Number");
            periodNumber.setValue(LocalDate.now().getMonthValue());

            DatePicker startDate = new DatePicker("Start Date");
            DatePicker endDate = new DatePicker("End Date");

            ComboBox<Fund> fundBox = new ComboBox<>("Fund");
            fundBox.setItems(fundRepository.findByOrderByCode());
            fundBox.setItemLabelGenerator(Fund::toString);
            fundBox.setClearButtonVisible(true);

            ComboBox<GrantAward> grantBox = new ComboBox<>("Grant");
            grantBox.setItems(grantRepository.findByOrderByCode());
            grantBox.setItemLabelGenerator(GrantAward::toString);
            grantBox.setClearButtonVisible(true);

            ComboBox<BudgetLine> budgetBox = new ComboBox<>("Budget Line");
            budgetBox.setItems(budgetLineRepository.findAll());
            budgetBox.setItemLabelGenerator(BudgetLine::getLabel);
            budgetBox.setClearButtonVisible(true);

            Button save = new Button("Save", event -> {
                try {
                    payrollService.createPeriod(
                            fiscalYear.getValue(),
                            periodNumber.getValue(),
                            startDate.getValue(),
                            endDate.getValue(),
                            fundBox.getValue() != null ? fundBox.getValue().getId() : null,
                            grantBox.getValue() != null ? grantBox.getValue().getId() : null,
                            budgetBox.getValue() != null ? budgetBox.getValue().getId() : null
                    );

                    grid.setItems(periodRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Payroll period created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    fiscalYear,
                    periodNumber,
                    startDate,
                    endDate,
                    fundBox,
                    grantBox,
                    budgetBox
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button calculateButton = new Button("Calculate Payroll", e -> {
            PayrollPeriod selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a payroll period.");
                return;
            }

            try {
                payrollService.calculatePayroll(selected.getId());
                grid.setItems(periodRepository.findByOrderByCreatedAtDesc());
                Notification.show("Payroll calculated.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button approveButton = new Button("Approve Payroll", e -> {
            PayrollPeriod selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a payroll period.");
                return;
            }

            try {
                payrollService.approvePayroll(selected.getId());
                grid.setItems(periodRepository.findByOrderByCreatedAtDesc());
                Notification.show("Payroll approved.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button payButton = new Button("Pay Payroll", e -> {
            PayrollPeriod selected = grid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a payroll period.");
                return;
            }

            Dialog dialog = new Dialog();

            ComboBox<TreasuryAccount> accountBox = new ComboBox<>("Treasury Account");
            accountBox.setItems(treasuryAccountRepository.findByActiveTrueOrderByCode());
            accountBox.setItemLabelGenerator(TreasuryAccount::toString);

            Button confirm = new Button("Pay", event -> {
                try {
                    if (accountBox.getValue() == null) {
                        Notification.show("Select a treasury account.");
                        return;
                    }

                    payrollService.payPayroll(selected.getId(), accountBox.getValue().getId());
                    grid.setItems(periodRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Payroll paid.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(accountBox);
            dialog.add(form, confirm);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e ->
                grid.setItems(periodRepository.findByOrderByCreatedAtDesc())
        );

        add(
                new H2("Payroll Periods"),
                new HorizontalLayout(newButton, calculateButton, approveButton, payButton, refreshButton),
                grid
        );
    }
}