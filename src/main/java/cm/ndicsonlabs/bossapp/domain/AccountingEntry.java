// src/main/java/com/institution/finance/domain/AccountingEntry.java
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
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "accounting_entry")
public class AccountingEntry extends BaseEntity {

    @Column(name = "entry_number", nullable = false, unique = true)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "accounting_period_id", nullable = false)
    private AccountingPeriod accountingPeriod;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "source_type")
    private String sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "original_entry_id")
    private UUID originalEntryId;

    @Column(nullable = false)
    private String status = "POSTED";

    @Column(name = "posted_by")
    private String postedBy;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "transaction_currency")
    private String transactionCurrency;

    @Column(name = "base_currency")
    private String baseCurrency;

    @Column(name = "exchange_rate", precision = 19, scale = 8)
    private BigDecimal exchangeRate;

    @Column(name = "reversed_by_entry_id")
    private UUID reversedByEntryId;

    @Column(name = "reversal_reason", columnDefinition = "TEXT")
    private String reversalReason;

    @Column(name = "auto_reversed", nullable = false)
    private boolean autoReversed = false;
}