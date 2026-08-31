// src/main/java/com/institution/finance/domain/BudgetLine.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "budget_line")
public class BudgetLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "budget_header_id", nullable = false)
    private BudgetHeader budgetHeader;

    @ManyToOne
    @JoinColumn(name = "account_code_id")
    private AccountCode accountCode;

    @Column(name = "expense_category")
    private String expenseCategory;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount = BigDecimal.ZERO;

    @Column(name = "adjusted_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal adjustedAmount = BigDecimal.ZERO;

    @Column(name = "reserved_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal reservedAmount = BigDecimal.ZERO;

    @Column(name = "spent_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal spentAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean active = true;

    @Transient
    public String getLabel() {
        String fundCode = budgetHeader != null && budgetHeader.getFund() != null
                ? budgetHeader.getFund().getCode()
                : "UNKNOWN";

        String category = expenseCategory != null ? expenseCategory : "NO_CATEGORY";

        return fundCode + " / " + category;
    }
}