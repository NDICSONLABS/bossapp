// src/main/java/com/institution/finance/domain/CostAllocationRun.java
package cm.ndicsonlabs.bossapp.domain.interdept;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
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
@Table(name = "cost_allocation_run")
public class CostAllocationRun extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private CostAllocationRule rule;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private String status = "POSTED";

    @Column(name = "posted_by")
    private String postedBy;
}