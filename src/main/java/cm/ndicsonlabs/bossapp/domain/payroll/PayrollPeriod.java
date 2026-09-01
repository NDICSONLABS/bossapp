// src/main/java/com/institution/finance/domain/PayrollPeriod.java
package cm.ndicsonlabs.bossapp.domain.payroll;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import cm.ndicsonlabs.bossapp.domain.Fund;
import cm.ndicsonlabs.bossapp.domain.GrantAward;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "payroll_period")
public class PayrollPeriod extends BaseEntity {

    @Column(name = "fiscal_year", nullable = false)
    private Integer fiscalYear;

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "fund_id")
    private Fund fund;

    @ManyToOne
    @JoinColumn(name = "grant_award_id")
    private GrantAward grantAward;

    @ManyToOne
    @JoinColumn(name = "budget_line_id")
    private BudgetLine budgetLine;

    @Column(name = "total_gross", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalGross = BigDecimal.ZERO;

    @Column(name = "total_deductions", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    @Column(name = "total_net", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalNet = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "prepared_by")
    private String preparedBy;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Override
    public String toString() {
        return fiscalYear + " P" + periodNumber;
    }
}