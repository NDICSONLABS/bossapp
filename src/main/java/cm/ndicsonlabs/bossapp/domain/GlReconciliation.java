// src/main/java/com/institution/finance/domain/GlReconciliation.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "gl_reconciliation")
public class GlReconciliation extends BaseEntity {

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "subledger_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal subledgerAmount = BigDecimal.ZERO;

    @Column(name = "gl_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal glAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "VARIANCE";

    @Column(columnDefinition = "TEXT")
    private String notes;
}