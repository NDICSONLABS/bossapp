// src/main/java/com/institution/finance/domain/InternalInvoice.java
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
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "internal_invoice")
public class InternalInvoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_department_id", nullable = false)
    private Department providerDepartment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_department_id", nullable = false)
    private Department receiverDepartment;

    @ManyToOne
    @JoinColumn(name = "service_id")
    private InternalServiceCatalog service;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status = "DRAFT";

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "posted_at")
    private Instant postedAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "created_by")
    private String createdBy;

    @Override
    public String toString() {
        return invoiceNumber;
    }
}