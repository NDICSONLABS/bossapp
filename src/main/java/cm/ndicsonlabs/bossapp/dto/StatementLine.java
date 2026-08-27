// src/main/java/com/institution/finance/report/StatementLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class StatementLine {

    private String section;
    private String line;
    private BigDecimal amount;

    public StatementLine(String section, String line, BigDecimal amount) {
        this.section = section;
        this.line = line;
        this.amount = amount;
    }

    public String getSection() {
        return section;
    }

    public String getLine() {
        return line;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}