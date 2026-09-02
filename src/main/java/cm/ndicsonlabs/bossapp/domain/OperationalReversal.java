// src/main/java/com/institution/finance/domain/OperationalReversal.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "operational_reversal")
public class OperationalReversal extends BaseEntity {

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @ManyToOne
    @JoinColumn(name = "accounting_entry_id")
    private AccountingEntry accountingEntry;

    @ManyToOne
    @JoinColumn(name = "reversal_entry_id")
    private AccountingEntry reversalEntry;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status = "DETECTED";

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt = Instant.now();

    @Column(name = "reversed_at")
    private Instant reversedAt;

    @Column(name = "reversed_by")
    private String reversedBy;

    @Column(nullable = false)
    private boolean automatic = false;
}