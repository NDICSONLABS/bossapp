// src/main/java/com/institution/finance/domain/AccountingEntryLine.java
package cm.ndicsonlabs.bossapp.domain;

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
@Table(name = "accounting_entry_line")
public class AccountingEntryLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "entry_id", nullable = false)
    private AccountingEntry entry;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_code_id", nullable = false)
    private AccountCode accountCode;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal debit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal credit = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String description;
}