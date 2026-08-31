// src/main/java/com/institution/finance/report/BudgetLineRow.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class BudgetLineRow {

    private String fundCode;
    private String grantCode;
    private String departmentName;
    private String expenseCategory;
    private BigDecimal originalAmount;
    private BigDecimal adjustedAmount;
    private BigDecimal reservedAmount;
    private BigDecimal spentAmount;
    private BigDecimal availableAmount;
    private String budgetStatus;

    public BudgetLineRow(
            String fundCode,
            String grantCode,
            String departmentName,
            String expenseCategory,
            BigDecimal originalAmount,
            BigDecimal adjustedAmount,
            BigDecimal reservedAmount,
            BigDecimal spentAmount,
            BigDecimal availableAmount,
            String budgetStatus
    ) {
        this.fundCode = fundCode;
        this.grantCode = grantCode;
        this.departmentName = departmentName;
        this.expenseCategory = expenseCategory;
        this.originalAmount = originalAmount;
        this.adjustedAmount = adjustedAmount;
        this.reservedAmount = reservedAmount;
        this.spentAmount = spentAmount;
        this.availableAmount = availableAmount;
        this.budgetStatus = budgetStatus;
    }

    public String getFundCode() {
        return fundCode;
    }

    public String getGrantCode() {
        return grantCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public BigDecimal getAdjustedAmount() {
        return adjustedAmount;
    }

    public BigDecimal getReservedAmount() {
        return reservedAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public String getBudgetStatus() {
        return budgetStatus;
    }
}