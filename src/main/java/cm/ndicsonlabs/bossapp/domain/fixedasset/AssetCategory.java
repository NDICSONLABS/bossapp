// src/main/java/com/institution/finance/domain/AssetCategory.java
package cm.ndicsonlabs.bossapp.domain.fixedasset;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "asset_category")
public class AssetCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "useful_life_months", nullable = false)
    private Integer usefulLifeMonths;

    @Column(name = "depreciation_method", nullable = false)
    private String depreciationMethod = "STRAIGHT_LINE";

    @Column(name = "salvage_value_percent", nullable = false, precision = 9, scale = 4)
    private BigDecimal salvageValuePercent = BigDecimal.ZERO;

    @Column(name = "capitalization_threshold", nullable = false, precision = 19, scale = 4)
    private BigDecimal capitalizationThreshold = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return code + " - " + name;
    }
}