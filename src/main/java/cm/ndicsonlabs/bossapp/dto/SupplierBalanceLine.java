// src/main/java/com/institution/finance/report/SupplierBalanceLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;

public class SupplierBalanceLine {

    private String supplierCode;
    private String supplierName;
    private String category;
    private BigDecimal totalInvoiced;
    private BigDecimal totalPaid;
    private BigDecimal outstanding;
    private BigDecimal overdue;

    public SupplierBalanceLine(
            String supplierCode,
            String supplierName,
            String category,
            BigDecimal totalInvoiced,
            BigDecimal totalPaid,
            BigDecimal outstanding,
            BigDecimal overdue
    ) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.category = category;
        this.totalInvoiced = totalInvoiced;
        this.totalPaid = totalPaid;
        this.outstanding = outstanding;
        this.overdue = overdue;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getTotalInvoiced() {
        return totalInvoiced;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public BigDecimal getOverdue() {
        return overdue;
    }
}