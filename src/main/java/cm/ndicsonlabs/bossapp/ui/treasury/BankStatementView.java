// src/main/java/com/institution/finance/ui/BankStatementView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankStatement;
import cm.ndicsonlabs.bossapp.domain.treasury.BankStatementLine;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementLineRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.service.treasury.BankReconciliationService;
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
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.time.LocalDate;

@Route(value = "bank-statements", layout = MainLayout.class)
@PermitAll
public class BankStatementView extends VerticalLayout {

    private final Grid<BankStatement> statementGrid = new Grid<>(BankStatement.class);
    private final Grid<BankStatementLine> lineGrid = new Grid<>(BankStatementLine.class);

    public BankStatementView(
            BankStatementRepository statementRepository,
            BankStatementLineRepository lineRepository,
            TreasuryAccountRepository accountRepository,
            BankReconciliationService reconciliationService
    ) {
        statementGrid.addColumn(statement -> statement.getTreasuryAccount().getCode()).setHeader("Account");
        statementGrid.setColumns(
                "statementNumber",
                "statementDate",
                "openingBalance",
                "closingBalance",
                "status"
        );
        statementGrid.setItems(statementRepository.findByOrderByCreatedAtDesc());

        lineGrid.setColumns(
                "lineNumber",
                "transactionDate",
                "direction",
                "amount",
                "reference",
                "description",
                "status"
        );

        statementGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
            } else {
                lineGrid.setItems(lineRepository.findByBankStatementIdOrderByLineNumberAsc(event.getValue().getId()));
            }
        });

        Button newStatementButton = new Button("New Statement", e -> {
            Dialog dialog = new Dialog();

            ComboBox<TreasuryAccount> accountBox = new ComboBox<>("Treasury Account");
            accountBox.setItems(accountRepository.findByActiveTrueOrderByCode());
            accountBox.setItemLabelGenerator(TreasuryAccount::toString);

            TextField statementNumber = new TextField("Statement Number");
            DatePicker statementDate = new DatePicker("Statement Date");
            BigDecimalField openingBalance = new BigDecimalField("Opening Balance");
            BigDecimalField closingBalance = new BigDecimalField("Closing Balance");

            Button save = new Button("Save", event -> {
                try {
                    reconciliationService.createStatement(
                            accountBox.getValue().getId(),
                            statementNumber.getValue(),
                            statementDate.getValue(),
                            openingBalance.getValue(),
                            closingBalance.getValue()
                    );

                    statementGrid.setItems(statementRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Bank statement created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    accountBox,
                    statementNumber,
                    statementDate,
                    openingBalance,
                    closingBalance
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button addLineButton = new Button("Add Statement Line", e -> {
            BankStatement selected = statementGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a bank statement.");
                return;
            }

            Dialog dialog = new Dialog();

            IntegerField lineNumber = new IntegerField("Line Number");
            DatePicker transactionDate = new DatePicker("Transaction Date");
            transactionDate.setValue(LocalDate.now());

            ComboBox<String> direction = new ComboBox<>("Direction");
            direction.setItems("IN", "OUT");
            direction.setValue("IN");

            BigDecimalField amount = new BigDecimalField("Amount");
            TextField reference = new TextField("Reference");
            TextArea description = new TextArea("Description");

            Button save = new Button("Save", event -> {
                try {
                    reconciliationService.addStatementLine(
                            selected.getId(),
                            lineNumber.getValue(),
                            transactionDate.getValue(),
                            amount.getValue(),
                            direction.getValue(),
                            reference.getValue(),
                            description.getValue()
                    );

                    lineGrid.setItems(lineRepository.findByBankStatementIdOrderByLineNumberAsc(selected.getId()));
                    dialog.close();
                    Notification.show("Statement line added.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(
                    lineNumber,
                    transactionDate,
                    direction,
                    amount,
                    reference,
                    description
            );

            dialog.add(form, save);
            dialog.open();
        });

        Button refreshButton = new Button("Refresh", e -> {
            statementGrid.setItems(statementRepository.findByOrderByCreatedAtDesc());

            BankStatement selected = statementGrid.asSingleSelect().getValue();

            if (selected != null) {
                lineGrid.setItems(lineRepository.findByBankStatementIdOrderByLineNumberAsc(selected.getId()));
            }
        });

        add(
                new H2("Bank Statements"),
                new HorizontalLayout(newStatementButton, addLineButton, refreshButton),
                statementGrid,
                new H2("Statement Lines"),
                lineGrid
        );
    }
}