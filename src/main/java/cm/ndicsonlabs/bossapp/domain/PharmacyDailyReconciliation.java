package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "pharmacy_daily_reconciliation")
public class PharmacyDailyReconciliation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "opening_supplier_credit", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingSupplierCredit = BigDecimal.ZERO;

    @Column(name = "new_supplier_invoices", nullable = false, precision = 19, scale = 4)
    private BigDecimal newSupplierInvoices = BigDecimal.ZERO;

    @Column(name = "supplier_payments", nullable = false, precision = 19, scale = 4)
    private BigDecimal supplierPayments = BigDecimal.ZERO;

    @Column(name = "expected_closing_credit", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedClosingCredit = BigDecimal.ZERO;

    @Column(name = "actual_closing_credit", precision = 19, scale = 4)
    private BigDecimal actualClosingCredit;

    @Column(precision = 19, scale = 4)
    private BigDecimal variance;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;
}
