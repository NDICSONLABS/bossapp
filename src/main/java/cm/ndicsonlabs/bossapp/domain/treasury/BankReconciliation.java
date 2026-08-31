// src/main/java/com/institution/finance/domain/BankReconciliation.java
package cm.ndicsonlabs.bossapp.domain.treasury;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
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
@Table(name = "bank_reconciliation")
public class BankReconciliation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "treasury_account_id", nullable = false)
    private TreasuryAccount treasuryAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_statement_id", nullable = false)
    private BankStatement bankStatement;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "statement_closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal statementClosingBalance = BigDecimal.ZERO;

    @Column(name = "cashbook_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cashbookBalance = BigDecimal.ZERO;

    @Column(name = "adjusted_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal adjustedBalance = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}