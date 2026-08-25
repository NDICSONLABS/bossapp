// src/main/java/com/institution/finance/report/CashFlowLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class CashFlowLine {

    private String flowDate;
    private String description;
    private String direction;
    private BigDecimal amount;
    private String source;

    public CashFlowLine(
            String flowDate,
            String description,
            String direction,
            BigDecimal amount,
            String source
    ) {
        this.flowDate = flowDate;
        this.description = description;
        this.direction = direction;
        this.amount = amount;
        this.source = source;
    }

    public String getFlowDate() {
        return flowDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getSource() {
        return source;
    }
}