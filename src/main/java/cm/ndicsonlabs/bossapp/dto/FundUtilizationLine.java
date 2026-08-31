// src/main/java/com/institution/finance/report/FundUtilizationLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class FundUtilizationLine {

    private String fundCode;
    private String fundName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal availableAmount;

    public FundUtilizationLine(
            String fundCode,
            String fundName,
            BigDecimal budgetAmount,
            BigDecimal spentAmount,
            BigDecimal availableAmount
    ) {
        this.fundCode = fundCode;
        this.fundName = fundName;
        this.budgetAmount = budgetAmount;
        this.spentAmount = spentAmount;
        this.availableAmount = availableAmount;
    }

    public String getFundCode() {
        return fundCode;
    }

    public String getFundName() {
        return fundName;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }
}