// src/main/java/com/institution/finance/domain/CashierSession.java
package cm.ndicsonlabs.bossapp.domain;

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
@Table(name = "cashier_session")
public class CashierSession extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "cashier_username", nullable = false)
    private String cashierUsername;

    @Column(name = "opening_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "expected_closing_balance", precision = 19, scale = 4)
    private BigDecimal expectedClosingBalance;

    @Column(name = "actual_closing_balance", precision = 19, scale = 4)
    private BigDecimal actualClosingBalance;

    @Column(precision = 19, scale = 4)
    private BigDecimal variance;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;
}