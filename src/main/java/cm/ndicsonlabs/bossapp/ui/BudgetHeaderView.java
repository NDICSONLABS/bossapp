// src/main/java/com/institution/finance/ui/BudgetHeaderView.java
package cm.ndicsonlabs.bossapp.ui;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.BudgetHeader;
import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import cm.ndicsonlabs.bossapp.repository.BudgetHeaderRepository;
import cm.ndicsonlabs.bossapp.repository.BudgetLineRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.FundRepository;
import cm.ndicsonlabs.bossapp.repository.GrantAwardRepository;
import cm.ndicsonlabs.bossapp.service.BudgetControlService;
import cm.ndicsonlabs.bossapp.ui.ux.BudgetLineSpreadsheet;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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

@Route(value = "budget-headers", layout = MainLayout.class)
@PermitAll
public class BudgetHeaderView extends VerticalLayout {

    private final Grid<BudgetHeader> headerGrid = new Grid<>(BudgetHeader.class);
    private final Grid<BudgetLine> lineGrid = new Grid<>(BudgetLine.class);

    public BudgetHeaderView(
            BudgetHeaderRepository headerRepository,
            BudgetLineRepository budgetLineRepository,
            FundRepository fundRepository,
            GrantAwardRepository grantRepository,
            DepartmentRepository departmentRepository,
            AccountCodeRepository accountCodeRepository,
            BudgetControlService budgetControlService
    ) {
        headerGrid.addColumn(header -> header.getFund().getCode()).setHeader("Fund");
        headerGrid.addColumn(header -> header.getGrantAward() != null
                ? header.getGrantAward().getCode()
                : "").setHeader("Grant");
        headerGrid.addColumn(header -> header.getDepartment().getName()).setHeader("Department");
        headerGrid.setColumns(
                "fiscalYear",
                "status",
                "totalAmount",
                "description"
        );
        headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());

        lineGrid.addColumn(line -> line.getAccountCode() != null
                ? line.getAccountCode().getCode()
                : "").setHeader("Account");
        lineGrid.addColumn(line -> budgetControlService.availableAmount(line)).setHeader("Available");
        lineGrid.setColumns(
                "expenseCategory",
                "description",
                "originalAmount",
                "adjustedAmount",
                "reservedAmount",
                "spentAmount"
        );

        headerGrid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() == null) {
                lineGrid.setItems();
            } else {
                lineGrid.setItems(budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(event.getValue().getId()));
            }
        });

        Button newHeaderButton = new Button("New Budget Header", e -> {
            Dialog dialog = new Dialog();

            IntegerField fiscalYear = new IntegerField("Fiscal Year");
            fiscalYear.setValue(LocalDate.now().getYear());

            ComboBox<Fund> fundBox = new ComboBox<>("Fund");
            fundBox.setItems(fundRepository.findByOrderByCode());
            fundBox.setItemLabelGenerator(Fund::toString);

            ComboBox<GrantAward> grantBox = new ComboBox<>("Grant");
            grantBox.setItems(grantRepository.findByOrderByCode());
            grantBox.setItemLabelGenerator(GrantAward::toString);
            grantBox.setClearButtonVisible(true);

            ComboBox<Department> departmentBox = new ComboBox<>("Department");
            departmentBox.setItems(departmentRepository.findAll());
            departmentBox.setItemLabelGenerator(Department::getName);

            TextArea description = new TextArea("Description");

            Button save = new Button("Save", event -> {
                try {
                    budgetControlService.createHeader(
                            fiscalYear.getValue(),
                            fundBox.getValue().getId(),
                            grantBox.getValue() != null ? grantBox.getValue().getId() : null,
                            departmentBox.getValue().getId(),
                            description.getValue()
                    );

                    headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());
                    dialog.close();
                    Notification.show("Budget header created.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(fiscalYear, fundBox, grantBox, departmentBox, description);
            dialog.add(form, save);
            dialog.open();
        });

        // Inside BudgetHeaderView.java constructor

// Remove the old "Add Budget Line" dialog button and replace with:
        Button editLinesBtn = new Button("Edit Budget Lines (Spreadsheet)", e -> {
            BudgetHeader selected = headerGrid.asSingleSelect().getValue();
            if (selected == null || !"DRAFT".equals(selected.getStatus())) {
                Notification.show("Select a draft budget header to edit lines.");
                return;
            }

            Dialog dialog = new Dialog();
            dialog.setWidth("1000px");
            dialog.setHeaderTitle("Budget Lines: " + selected.getFund().getCode());

            BudgetLineSpreadsheet spreadsheetEditor = new BudgetLineSpreadsheet(
                    accountCodeRepository,
                    parsedLines -> {
                        // 1. Delete existing lines for this header (or use a smart diffing service)
                        budgetLineRepository.deleteByBudgetHeaderId(selected.getId());

                        // 2. Save new lines
                        parsedLines.forEach(line -> {
                            line.setBudgetHeader(selected);
                            budgetLineRepository.save(line);
                        });

                        // 3. Refresh UI
                        lineGrid.setItems(budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(selected.getId()));
                        dialog.close();
                    }
            );

            // Load existing lines into the spreadsheet
            spreadsheetEditor.loadData(budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(selected.getId()));

            dialog.add(spreadsheetEditor);
            dialog.open();
        });

        Button addLineButton = new Button("Add Budget Line", e -> {
            BudgetHeader selected = headerGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a budget header.");
                return;
            }

            Dialog dialog = new Dialog();

            ComboBox<AccountCode> accountBox = new ComboBox<>("Account Code");
            accountBox.setItems(accountCodeRepository.findByOrderByCode());
            accountBox.setItemLabelGenerator(account -> account.getCode() + " - " + account.getName());
            accountBox.setClearButtonVisible(true);

            TextField expenseCategory = new TextField("Expense Category");
            TextArea description = new TextArea("Description");
            BigDecimalField amount = new BigDecimalField("Amount");

            Button save = new Button("Save", event -> {
                try {
                    budgetControlService.addLine(
                            selected.getId(),
                            accountBox.getValue() != null ? accountBox.getValue().getId() : null,
                            expenseCategory.getValue(),
                            description.getValue(),
                            amount.getValue()
                    );

                    lineGrid.setItems(budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(selected.getId()));
                    dialog.close();
                    Notification.show("Budget line added.");
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            });

            FormLayout form = new FormLayout(accountBox, expenseCategory, description, amount);
            dialog.add(form, save);
            dialog.open();
        });

        Button submitButton = new Button("Submit Budget", e -> {
            BudgetHeader selected = headerGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a budget header.");
                return;
            }

            try {
                budgetControlService.submitHeader(selected.getId());
                headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());
                Notification.show("Budget submitted.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button approveButton = new Button("Approve Budget", e -> {
            BudgetHeader selected = headerGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a budget header.");
                return;
            }

            try {
                budgetControlService.approveHeader(selected.getId());
                headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());
                Notification.show("Budget approved.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button lockButton = new Button("Lock Budget", e -> {
            BudgetHeader selected = headerGrid.asSingleSelect().getValue();

            if (selected == null) {
                Notification.show("Select a budget header.");
                return;
            }

            try {
                budgetControlService.lockHeader(selected.getId());
                headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());
                Notification.show("Budget locked.");
            } catch (Exception ex) {
                Notification.show(ex.getMessage());
            }
        });

        Button refreshButton = new Button("Refresh", e -> {
            headerGrid.setItems(headerRepository.findByOrderByCreatedAtDesc());

            BudgetHeader selected = headerGrid.asSingleSelect().getValue();

            if (selected != null) {
                lineGrid.setItems(budgetLineRepository.findByBudgetHeaderIdOrderByCreatedAtAsc(selected.getId()));
            }
        });

        add(
                new H2("Budget Headers"),
                new HorizontalLayout(newHeaderButton, addLineButton, editLinesBtn, submitButton, approveButton, lockButton, refreshButton),
                headerGrid,
                new H2("Budget Lines"),
                lineGrid
        );
    }
}