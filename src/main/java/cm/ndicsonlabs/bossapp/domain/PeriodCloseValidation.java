// src/main/java/com/institution/finance/domain/PeriodCloseValidation.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "period_close_validation")
public class PeriodCloseValidation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private AccountingPeriod period;

    @Column(name = "validation_code", nullable = false)
    private String validationCode;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;
}