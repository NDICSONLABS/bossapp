// src/main/java/com/institution/finance/domain/GlReconciliationLine.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "gl_reconciliation_line")
public class GlReconciliationLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "gl_reconciliation_id", nullable = false)
    private GlReconciliation glReconciliation;

    @ManyToOne
    @JoinColumn(name = "account_code_id")
    private AccountCode accountCode;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @ManyToOne
    @JoinColumn(name = "accounting_entry_id")
    private AccountingEntry accountingEntry;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(columnDefinition = "TEXT")
    private String notes;
}