// src/main/java/com/institution/finance/report/AgingLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class AgingLine {

    private String entityName;
    private String reference;
    private String dueDate;
    private BigDecimal originalAmount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private Long overdueDays;
    private String agingBucket;

    public AgingLine(
            String entityName,
            String reference,
            String dueDate,
            BigDecimal originalAmount,
            BigDecimal paidAmount,
            BigDecimal outstandingAmount,
            Long overdueDays,
            String agingBucket
    ) {
        this.entityName = entityName;
        this.reference = reference;
        this.dueDate = dueDate;
        this.originalAmount = originalAmount;
        this.paidAmount = paidAmount;
        this.outstandingAmount = outstandingAmount;
        this.overdueDays = overdueDays;
        this.agingBucket = agingBucket;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getReference() {
        return reference;
    }

    public String getDueDate() {
        return dueDate;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public Long getOverdueDays() {
        return overdueDays;
    }

    public String getAgingBucket() {
        return agingBucket;
    }
}