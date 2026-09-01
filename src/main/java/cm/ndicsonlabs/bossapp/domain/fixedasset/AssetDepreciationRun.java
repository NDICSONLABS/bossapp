// src/main/java/com/institution/finance/domain/AssetDepreciationRun.java
package cm.ndicsonlabs.bossapp.domain.fixedasset;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
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
@Table(name = "asset_depreciation_run")
public class AssetDepreciationRun extends BaseEntity {

    @Column(name = "run_date", nullable = false)
    private LocalDate runDate;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "total_depreciation", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDepreciation = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "COMPLETED";

    @Column(name = "posted_by")
    private String postedBy;
}