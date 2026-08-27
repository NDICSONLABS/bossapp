// src/main/java/com/institution/finance/domain/SupplierStatementReconciliation.java
package cm.ndicsonlabs.bossapp.domain;

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
@Table(name = "supplier_statement_reconciliation")
public class SupplierStatementReconciliation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "supplier_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal supplierBalance = BigDecimal.ZERO;

    @Column(name = "system_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal systemBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "VARIANCE";

    @Column(columnDefinition = "TEXT")
    private String notes;
}