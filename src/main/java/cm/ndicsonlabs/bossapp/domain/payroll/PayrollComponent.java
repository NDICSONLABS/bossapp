// src/main/java/com/institution/finance/domain/PayrollComponent.java
package cm.ndicsonlabs.bossapp.domain.payroll;

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
@Table(name = "payroll_component")
public class PayrollComponent extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "component_type", nullable = false)
    private String componentType;

    @Column(name = "calculation_type", nullable = false)
    private String calculationType;

    @Column(name = "default_amount", precision = 19, scale = 4)
    private BigDecimal defaultAmount;

    @Column(name = "default_percent", precision = 9, scale = 4)
    private BigDecimal defaultPercent;

    @Column(nullable = false)
    private boolean taxable = false;

    @Column(nullable = false)
    private boolean statutory = false;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return code + " - " + name;
    }
}