// src/main/java/com/institution/finance/report/GrantUtilizationLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class GrantUtilizationLine {

    private String grantCode;
    private String grantName;
    private String donorName;
    private BigDecimal awardAmount;
    private BigDecimal allocatedBudget;
    private BigDecimal spentAmount;
    private BigDecimal remainingBudget;

    public GrantUtilizationLine(
            String grantCode,
            String grantName,
            String donorName,
            BigDecimal awardAmount,
            BigDecimal allocatedBudget,
            BigDecimal spentAmount,
            BigDecimal remainingBudget
    ) {
        this.grantCode = grantCode;
        this.grantName = grantName;
        this.donorName = donorName;
        this.awardAmount = awardAmount;
        this.allocatedBudget = allocatedBudget;
        this.spentAmount = spentAmount;
        this.remainingBudget = remainingBudget;
    }

    public String getGrantCode() {
        return grantCode;
    }

    public String getGrantName() {
        return grantName;
    }

    public String getDonorName() {
        return donorName;
    }

    public BigDecimal getAwardAmount() {
        return awardAmount;
    }

    public BigDecimal getAllocatedBudget() {
        return allocatedBudget;
    }

    public BigDecimal getSpentAmount() {
        return spentAmount;
    }

    public BigDecimal getRemainingBudget() {
        return remainingBudget;
    }
}