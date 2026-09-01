// src/main/java/com/institution/finance/domain/FixedAsset.java
package cm.ndicsonlabs.bossapp.domain.fixedasset;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "fixed_asset")
public class FixedAsset extends BaseEntity {

    @Column(name = "asset_number", nullable = false, unique = true)
    private String assetNumber;

    @Column(nullable = false)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private AssetCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "custodian_employee_id")
    private Employee custodianEmployee;

    @Column(name = "physical_location")
    private String physicalLocation;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Column(name = "capitalized_date")
    private LocalDate capitalizedDate;

    @Column(name = "original_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalCost = BigDecimal.ZERO;

    @Column(name = "salvage_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal salvageValue = BigDecimal.ZERO;

    @Column(name = "accumulated_depreciation", nullable = false, precision = 19, scale = 4)
    private BigDecimal accumulatedDepreciation = BigDecimal.ZERO;

    @Column(name = "net_book_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal netBookValue = BigDecimal.ZERO;

    @Column(name = "depreciation_method", nullable = false)
    private String depreciationMethod;

    @Column(name = "useful_life_months", nullable = false)
    private Integer usefulLifeMonths;

    @Column(nullable = false)
    private String status = "ACQUIRED";

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "supplier_invoice_id")
    private SupplierInvoice supplierInvoice;

    @Override
    public String toString() {
        return assetNumber + " - " + description;
    }
}