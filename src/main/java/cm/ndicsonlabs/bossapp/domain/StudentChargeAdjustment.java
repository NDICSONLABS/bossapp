// src/main/java/com/institution/finance/domain/StudentChargeAdjustment.java
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

@Getter
@Setter
@Entity
@Table(name = "student_charge_adjustment")
public class StudentChargeAdjustment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_charge_id", nullable = false)
    private StudentCharge studentCharge;

    @Column(name = "adjustment_type", nullable = false)
    private String adjustmentType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(nullable = false)
    private String status = "APPROVED";
}