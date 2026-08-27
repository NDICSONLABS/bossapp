package cm.ndicsonlabs.bossapp.dto;


import java.math.BigDecimal;
import java.util.UUID;

public class SupplierCreditSummary {

    private UUID supplierId;
    private String supplierCode;
    private String supplierName;
    private String category;
    private String subcategory;
    private BigDecimal creditLimit;
    private BigDecimal outstanding;
    private BigDecimal availableCredit;
    private BigDecimal utilizationPercent;
    private boolean creditHold;

    public SupplierCreditSummary(
            UUID supplierId,
            String supplierCode,
            String supplierName,
            String category,
            String subcategory,
            BigDecimal creditLimit,
            BigDecimal outstanding,
            BigDecimal availableCredit,
            BigDecimal utilizationPercent,
            boolean creditHold
    ) {
        this.supplierId = supplierId;
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
        this.category = category;
        this.subcategory = subcategory;
        this.creditLimit = creditLimit;
        this.outstanding = outstanding;
        this.availableCredit = availableCredit;
        this.utilizationPercent = utilizationPercent;
        this.creditHold = creditHold;
    }

    public UUID getSupplierId() {
        return supplierId;
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

    public String getSubcategory() {
        return subcategory;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getOutstanding() {
        return outstanding;
    }

    public BigDecimal getAvailableCredit() {
        return availableCredit;
    }

    public BigDecimal getUtilizationPercent() {
        return utilizationPercent;
    }

    public boolean isCreditHold() {
        return creditHold;
    }
}