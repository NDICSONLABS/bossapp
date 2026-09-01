// src/main/java/com/institution/finance/domain/AssetDisposal.java
package cm.ndicsonlabs.bossapp.domain.fixedasset;

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
@Table(name = "asset_disposal")
public class AssetDisposal extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @Column(name = "disposal_date", nullable = false)
    private LocalDate disposalDate;

    @Column(name = "disposal_type", nullable = false)
    private String disposalType; // SOLD, SCRAPPED, DONATED, WRITTEN_OFF

    @Column(precision = 19, scale = 4)
    private BigDecimal proceeds = BigDecimal.ZERO;

    @Column(name = "net_book_value_at_disposal", nullable = false, precision = 19, scale = 4)
    private BigDecimal netBookValueAtDisposal = BigDecimal.ZERO;

    @Column(name = "gain_or_loss", nullable = false, precision = 19, scale = 4)
    private BigDecimal gainOrLoss = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_by")
    private String approvedBy;
}