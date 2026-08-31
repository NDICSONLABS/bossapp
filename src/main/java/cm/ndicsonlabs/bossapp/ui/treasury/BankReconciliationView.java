// src/main/java/com/institution/finance/ui/BankReconciliationView.java
package cm.ndicsonlabs.bossapp.ui.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankReconciliation;
import cm.ndicsonlabs.bossapp.domain.treasury.BankStatement;
import cm.ndicsonlabs.bossapp.domain.treasury.BankStatementLine;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import cm.ndicsonlabs.bossapp.repository.treasury.BankReconciliationRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementLineRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementRepository;
import cm.ndicsonlabs.bossapp.service.treasury.BankReconciliationService;
import cm.ndicsonlabs.bossapp.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "bank-reconciliation", layout = MainLayout.class)
@PermitAll
public class BankReconciliationView extends VerticalLayout {

    private final Grid<BankReconciliation> reconciliationGrid = new Grid<>(BankReconciliation.class);
    private final Grid<BankStatementLine> lineGrid = new Grid<>(BankStatementLine.class);
    private final Grid<TreasuryTransaction> transactionGrid = new Grid<>(TreasuryTransaction.class);

    public BankReconciliationView(
            BankStatementRepository statementRepository,
            BankReconciliationRepository reconciliationRepository,
            BankStatementLineRepository lineRepository,
            BankReconciliationService reconciliationService
    ) {
        ComboBox<BankStatement> statementBox = new ComboBox<>("Bank Statement");
        statementBox.setItems(statementRepository.findByOrderByCreatedAtDesc());
        statementBox.setItemLabelGenerator(BankStatement::toString);

        reconciliationGrid.addColumn(reconciliation -> reconciliation.getTreasuryAccount().getCode()).setHeader("Account");
        reconciliationGrid.setColumns(
                "statementDate",
                "statementClosingBalance",
                "cashbookBalance",
                "adjustedBalance",
                "variance",
                "status",
                "preparedBy",
                "approvedBy"
        );
        reconciliationGrid.setItems(reconciliationRepository.findByOrderByCreatedAtDesc());

        lineGrid.setColumns(
                "lineNumber",
                "transactionDate",
                "direction",
                "amount",
                "reference",
                "description",
                "status"
        );

        transactionGrid.setColumns(
                "transactionNumber",
                "transactionDate",
                "direction",
                "amount",
                "reference",
                "status"
        );

        reconciliationGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
                transactionGrid.setItems();
            } else {
                BankReconciliation reconciliation = event.getValue();

                lineGrid.setItems(reconciliationService.unmatchedLines(reconciliation.getBankStatement().getId()));

                transactionGrid.setItems(reconciliationService.unmatchedTransactions(
                        reconciliation.getTreasuryAccount().getId(),
                        reconciliation.getStatementDate()
                ));
            }
        });

        Button prepareButton = new Button("Prepare Reconciliation", e -> {
            if (statementBox.getValue() == null) {
                Notification.show("Select a bank statement.");
                return;
            }

            try {
                reconciliationService.prepareReconciliation(statementBox.getValue().getId());
                reconciliationGrid.setItems(reconciliationRepository.findByOrderByCreatedAtDesc());
                Notification.show("Reconciliation prepared.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button matchButton = new Button("Match Selected Line and Transaction", e -> {
            BankStatementLine selectedLine = lineGrid.asSingleSelect().getValue();
            TreasuryTransaction selectedTransaction = transactionGrid.asSingleSelect().getValue();
            BankReconciliation selectedReconciliation = reconciliationGrid.asSingleSelect().getValue();

            if (selectedLine == null || selectedTransaction == null || selectedReconciliation == null) {
                Notification.show("Select a reconciliation, statement line, and treasury transaction.");
                return;
            }

            try {
                reconciliationService.matchLine(selectedLine.getId(), selectedTransaction.getId());

                lineGrid.setItems(reconciliationService.unmatchedLines(selectedReconciliation.getBankStatement().getId()));

                transactionGrid.setItems(reconciliationService.unmatchedTransactions(
                        selectedReconciliation.getTreasuryAccount().getId(),
                        selectedReconciliation.getStatementDate()
                ));

                Notification.show("Statement line matched.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button ignoreButton = new Button("Ignore Selected Line", e -> {
            BankStatementLine selectedLine = lineGrid.asSingleSelect().getValue();
            BankReconciliation selectedReconciliation = reconciliationGrid.asSingleSelect().getValue();

            if (selectedLine == null || selectedReconciliation == null) {
                Notification.show("Select a reconciliation and statement line.");
                return;
            }

            Dialog dialog = new Dialog();
            TextArea reason = new TextArea("Ignore Reason");

            Button confirm = new Button("Ignore Line", event -> {
                try {
                    reconciliationService.ignoreLine(selectedLine.getId(), reason.getValue());

                    lineGrid.setItems(reconciliationService.unmatchedLines(selectedReconciliation.getBankStatement().getId()));

                    dialog.close();
                    Notification.show("Statement line ignored.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(reason);
            dialog.add(form, confirm);
            dialog.open();
        });

        Button completeButton = new Button("Complete Reconciliation", e -> {
            BankReconciliation selected = reconciliationGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a reconciliation.");
                return;
            }

            try {
                reconciliationService.completeReconciliation(selected.getId());
                reconciliationGrid.setItems(reconciliationRepository.findByOrderByCreatedAtDesc());
                Notification.show("Reconciliation completed.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button approveButton = new Button("Approve Reconciliation", e -> {
            BankReconciliation selected = reconciliationGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a reconciliation.");
                return;
            }

            try {
                reconciliationService.approveReconciliation(selected.getId());
                reconciliationGrid.setItems(reconciliationRepository.findByOrderByCreatedAtDesc());
                Notification.show("Reconciliation approved.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e -> {
            reconciliationGrid.setItems(reconciliationRepository.findByOrderByCreatedAtDesc());

            BankReconciliation selected = reconciliationGrid.asSingleSelect().getValue();

            if (selected != null) {
                lineGrid.setItems(reconciliationService.unmatchedLines(selected.getBankStatement().getId()));

                transactionGrid.setItems(reconciliationService.unmatchedTransactions(
                        selected.getTreasuryAccount().getId(),
                        selected.getStatementDate()
                ));
            }
        });

        add(
                new H2("Bank Reconciliation"),
                new HorizontalLayout(statementBox, prepareButton, matchButton, ignoreButton, completeButton, approveButton, refreshButton),
                reconciliationGrid,
                new H2("Unmatched Statement Lines"),
                lineGrid,
                new H2("Unmatched Treasury Transactions"),
                transactionGrid
        );
    }
}