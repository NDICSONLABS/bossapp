// src/main/java/com/institution/finance/domain/CostAllocationRuleTarget.java
package cm.ndicsonlabs.bossapp.domain.interdept;

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
@Table(name = "cost_allocation_rule_target")
public class CostAllocationRuleTarget extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private CostAllocationRule rule;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_department_id", nullable = false)
    private Department receiverDepartment;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal percentage;
}