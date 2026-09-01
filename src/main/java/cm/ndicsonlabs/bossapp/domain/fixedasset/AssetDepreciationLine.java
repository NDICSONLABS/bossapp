// src/main/java/com/institution/finance/domain/AssetDepreciationLine.java
package cm.ndicsonlabs.bossapp.domain.fixedasset;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import cm.ndicsonlabs.bossapp.domain.Department;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "asset_depreciation_line")
public class AssetDepreciationLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private AssetDepreciationRun run;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private FixedAsset asset;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "depreciation_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal depreciationAmount = BigDecimal.ZERO;
}