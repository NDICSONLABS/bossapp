// src/main/java/com/institution/finance/domain/InternalSettlement.java
package cm.ndicsonlabs.bossapp.domain.interdept;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import cm.ndicsonlabs.bossapp.domain.Department;
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
@Table(name = "internal_settlement")
public class InternalSettlement extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_department_id", nullable = false)
    private Department providerDepartment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_department_id", nullable = false)
    private Department receiverDepartment;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String reference;

    @Column(nullable = false)
    private String status = "POSTED";

    @Column(name = "posted_by")
    private String postedBy;
}