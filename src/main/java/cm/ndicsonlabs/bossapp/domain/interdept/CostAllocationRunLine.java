// src/main/java/com/institution/finance/domain/CostAllocationRunLine.java
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
@Table(name = "cost_allocation_run_line")
public class CostAllocationRunLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CostAllocationRun run;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_department_id", nullable = false)
    private Department receiverDepartment;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal percentage;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
}