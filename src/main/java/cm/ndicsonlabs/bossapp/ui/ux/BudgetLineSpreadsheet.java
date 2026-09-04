package cm.ndicsonlabs.bossapp.ui.ux;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheet;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetColumn;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetOptions;
import cm.ndicsonlabs.bossapp.ui.ux.spreadsheet.JSpreadsheetWorksheet;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BudgetLineSpreadsheet extends VerticalLayout {

    private final JSpreadsheet spreadsheet;
    private final AccountCodeRepository accountCodeRepository;
    private final Consumer<List<BudgetLine>> onSave;
    
    private List<AccountCode> accountCodes;
    private Map<String, UUID> accountCodeMap; // Display Name -> UUID

    public BudgetLineSpreadsheet(AccountCodeRepository accountCodeRepository, Consumer<List<BudgetLine>> onSave) {
        this.accountCodeRepository = accountCodeRepository;
        this.onSave = onSave;
        
        this.accountCodes = accountCodeRepository.findByOrderByCode();
        this.accountCodeMap = accountCodes.stream()
                .collect(Collectors.toMap(a -> a.getCode() + " - " + a.getName(), AccountCode::getId));

        spreadsheet = new JSpreadsheet();
        spreadsheet.setHeight("450px");
        spreadsheet.setWidthFull();
        
        configureSpreadsheet();
        
        Button saveBtn = new Button("Save Budget Lines", e -> extractAndSave());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        
        Button addRowBtn = new Button("Add Row", e -> spreadsheet.insertRow(-1, 1));
        Button delRowBtn = new Button("Delete Selected Row", e -> {
            // Note: In a real app, you'd get the selected row index from the JS side.
            // For simplicity, we delete the last row or rely on the context menu.
            spreadsheet.deleteRow(0, 1); 
        });
        
        HorizontalLayout toolbar = new HorizontalLayout(addRowBtn, delRowBtn, saveBtn);
        toolbar.setAlignItems(Alignment.BASELINE);
        toolbar.setWidthFull();
        
        add(toolbar, spreadsheet);
    }

    private void configureSpreadsheet() {
        JSpreadsheetWorksheet worksheet = new JSpreadsheetWorksheet();
        worksheet.setWorksheetName("Budget Lines");
        worksheet.setMinSpareRows(10);

        List<JSpreadsheetColumn> columns = new ArrayList<>();
        
        // Col 0: Account Code (Dropdown)
        List<Object> accountSources = accountCodes.stream()
                .map(a -> a.getCode() + " - " + a.getName())
                .collect(Collectors.toList());
        columns.add(JSpreadsheetColumn.dropdown("Account Code", 250, accountSources));
        
        // Col 1: Expense Category (Text)
        columns.add(JSpreadsheetColumn.text("Expense Category", 150));
        
        // Col 2: Description (Text)
        columns.add(JSpreadsheetColumn.text("Description", 250));
        
        // Col 3: Original Amount (Numeric)
        JSpreadsheetColumn amountCol = JSpreadsheetColumn.numeric("Original Amount", 120);
        amountCol.setMask("$ #.##0,00"); // jsuites mask format
        columns.add(amountCol);

        worksheet.setColumns(columns);
        
        JSpreadsheetOptions options = JSpreadsheetOptions.withSingleWorksheet(worksheet);
        spreadsheet.setOptions(options);
    }

    public void loadData(List<BudgetLine> lines) {
        List<List<Object>> data = new ArrayList<>();
        for (BudgetLine line : lines) {
            List<Object> row = new ArrayList<>();
            row.add(line.getAccountCode() != null ? line.getAccountCode().getCode() + " - " + line.getAccountCode().getName() : "");
            row.add(line.getExpenseCategory() != null ? line.getExpenseCategory() : "");
            row.add(line.getDescription() != null ? line.getDescription() : "");
            row.add(line.getOriginalAmount() != null ? line.getOriginalAmount().doubleValue() : 0.0);
            data.add(row);
        }
        spreadsheet.setData(data);
    }

    private void extractAndSave() {
        // Async call to browser to get current state
        spreadsheet.getData().thenAccept(data -> {
            getUI().ifPresent(ui -> ui.access(() -> {
                List<BudgetLine> parsedLines = new ArrayList<>();
                int rowIndex = 1;
                
                for (List<Object> row : data) {
                    rowIndex++;
                    if (row == null || row.isEmpty() || isRowEmpty(row)) continue;

                    BudgetLine line = new BudgetLine();
                    
                    // Col 0: Account
                    String accStr = getString(row, 0);
                    if (accStr != null && accountCodeMap.containsKey(accStr)) {
                        AccountCode acc = new AccountCode();
                        acc.setId(accountCodeMap.get(accStr));
                        line.setAccountCode(acc);
                    } else if (accStr != null && !accStr.isBlank()) {
                        Notification.show("Row " + rowIndex + ": Invalid Account Code");
                        return;
                    }

                    line.setExpenseCategory(getString(row, 1));
                    line.setDescription(getString(row, 2));
                    
                    Double amount = getDouble(row, 3);
                    if (amount == null || amount <= 0) {
                        Notification.show("Row " + rowIndex + ": Amount must be greater than zero");
                        return;
                    }
                    line.setOriginalAmount(BigDecimal.valueOf(amount));

                    parsedLines.add(line);
                }
                
                if (onSave != null) {
                    onSave.accept(parsedLines);
                    Notification.show("Saved " + parsedLines.size() + " budget lines.");
                }
            }));
        }).exceptionally(ex -> {
            getUI().ifPresent(ui -> ui.access(() -> Notification.show("Error reading spreadsheet: " + ex.getMessage())));
            return null;
        });
    }

    private boolean isRowEmpty(List<Object> row) {
        return row.stream().allMatch(c -> c == null || c.toString().isBlank());
    }

    private String getString(List<Object> row, int index) {
        if (row.size() > index && row.get(index) != null) return row.get(index).toString().trim();
        return null;
    }

    private Double getDouble(List<Object> row, int index) {
        if (row.size() > index && row.get(index) != null) {
            Object val = row.get(index);
            if (val instanceof Number) return ((Number) val).doubleValue();
            try { return Double.parseDouble(val.toString().replace(",", "").replace("$", "")); } 
            catch (Exception e) { return null; }
        }
        return null;
    }
}