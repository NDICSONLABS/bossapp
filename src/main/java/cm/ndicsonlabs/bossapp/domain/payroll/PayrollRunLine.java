// src/main/java/com/institution/finance/domain/PayrollRunLine.java
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

@Getter
@Setter
@Entity
@Table(name = "payroll_run_line")
public class PayrollRunLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_payroll_run_id", nullable = false)
    private EmployeePayrollRun employeePayrollRun;

    @ManyToOne(optional = false)
    @JoinColumn(name = "payroll_component_id", nullable = false)
    private PayrollComponent payrollComponent;

    @Column(name = "line_type", nullable = false)
    private String lineType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;
}