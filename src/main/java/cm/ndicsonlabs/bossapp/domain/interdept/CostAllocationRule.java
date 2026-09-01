// src/main/java/com/institution/finance/domain/CostAllocationRule.java
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

@Getter
@Setter
@Entity
@Table(name = "cost_allocation_rule")
public class CostAllocationRule extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_department_id", nullable = false)
    private Department sourceDepartment;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return name;
    }
}