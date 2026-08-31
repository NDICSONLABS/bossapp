// src/main/java/com/institution/finance/domain/BankStatement.java
package cm.ndicsonlabs.bossapp.domain.treasury;

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
@Table(name = "bank_statement")
public class BankStatement extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "treasury_account_id", nullable = false)
    private TreasuryAccount treasuryAccount;

    @Column(name = "statement_number", nullable = false)
    private String statementNumber;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "closing_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingBalance = BigDecimal.ZERO;

    private String currency;

    @Column(nullable = false)
    private String status = "UPLOADED";

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Override
    public String toString() {
        return statementNumber + " - " + statementDate;
    }
}