// src/main/java/com/institution/finance/domain/EmployeePayrollRun.java
package cm.ndicsonlabs.bossapp.domain.payroll;

import cm.ndicsonlabs.bossapp.domain.*;
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
@Table(name = "employee_payroll_run")
public class EmployeePayrollRun extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "payroll_period_id", nullable = false)
    private PayrollPeriod payrollPeriod;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "grant_award_id")
    private GrantAward grantAward;

    @ManyToOne
    @JoinColumn(name = "budget_line_id")
    private BudgetLine budgetLine;

    @Column(name = "gross_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grossAmount = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "CALCULATED";
}