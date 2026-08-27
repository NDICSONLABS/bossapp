// src/main/java/com/institution/finance/domain/PurchaseRequest.java
package cm.ndicsonlabs.bossapp.domain;

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
@Table(name = "purchase_request")
public class PurchaseRequest extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "request_date")
    private LocalDate requestDate;

    @Column(name = "needed_by")
    private LocalDate neededBy;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_amount", precision = 19, scale = 4)
    private BigDecimal estimatedAmount;

    @Column(nullable = false)
    private String status = "SUBMITTED";
}