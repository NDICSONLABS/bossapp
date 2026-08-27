package cm.ndicsonlabs.bossapp.domain;


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
@Table(name = "supplier_credit_control")
public class SupplierCreditControl extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Column(name = "credit_terms_days")
    private Integer creditTermsDays;

    @Column(name = "alert_threshold_days", nullable = false)
    private Integer alertThresholdDays = 7;

    @Column(name = "hold_on_limit_exceeded", nullable = false)
    private boolean holdOnLimitExceeded = false;

    @Column(nullable = false)
    private boolean active = true;
}