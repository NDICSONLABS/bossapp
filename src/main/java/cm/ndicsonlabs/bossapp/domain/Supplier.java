package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier")
public class Supplier extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;

    private String category;

    @Column(name = "payment_terms_days")
    private Integer paymentTermsDays;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Column(name = "supplier_subcategory")
    private String supplierSubcategory;

    @Column(name = "credit_hold", nullable = false)
    private boolean creditHold = false;

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getSupplierSubcategory() {
        return supplierSubcategory;
    }

    public void setSupplierSubcategory(String supplierSubcategory) {
        this.supplierSubcategory = supplierSubcategory;
    }

    public boolean isCreditHold() {
        return creditHold;
    }

    public void setCreditHold(boolean creditHold) {
        this.creditHold = creditHold;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPaymentTermsDays() {
        return paymentTermsDays;
    }

    public void setPaymentTermsDays(Integer paymentTermsDays) {
        this.paymentTermsDays = paymentTermsDays;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
