// src/main/java/com/institution/finance/domain/EmployeeSalaryComponent.java
package cm.ndicsonlabs.bossapp.domain.payroll;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "employee_salary_component")
public class EmployeeSalaryComponent extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payroll_component_id", nullable = false)
    private PayrollComponent payrollComponent;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 9, scale = 4)
    private BigDecimal percentage;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean active = true;
}