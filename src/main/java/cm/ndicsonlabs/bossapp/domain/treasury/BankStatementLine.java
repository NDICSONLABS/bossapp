// src/main/java/com/institution/finance/domain/BankStatementLine.java
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
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "bank_statement_line")
public class BankStatementLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "bank_statement_id", nullable = false)
    private BankStatement bankStatement;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String direction;

    private String reference;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "matched_treasury_transaction_id")
    private TreasuryTransaction matchedTreasuryTransaction;

    @Column(nullable = false)
    private String status = "UNMATCHED";

    @Column(name = "ignore_reason", columnDefinition = "TEXT")
    private String ignoreReason;
}