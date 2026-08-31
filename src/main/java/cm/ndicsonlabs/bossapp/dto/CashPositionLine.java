// src/main/java/com/institution/finance/report/CashPositionLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class CashPositionLine {

    private String departmentName;
    private int accountCount;
    private BigDecimal totalBalance;

    public CashPositionLine(String departmentName, int accountCount, BigDecimal totalBalance) {
        this.departmentName = departmentName;
        this.accountCount = accountCount;
        this.totalBalance = totalBalance;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public int getAccountCount() {
        return accountCount;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }
}