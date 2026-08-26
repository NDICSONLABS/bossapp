// src/main/java/com/institution/finance/domain/CashierTransaction.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "cashier_transaction")
public class CashierTransaction extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cashier_session_id", nullable = false)
    private CashierSession cashierSession;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(nullable = false)
    private String direction;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String description;
}