// src/main/java/com/institution/finance/domain/StudentPaymentPlan.java
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
@Table(name = "student_payment_plan")
public class StudentPaymentPlan extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "total_debt", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalDebt;

    @Column(name = "down_payment", nullable = false, precision = 19, scale = 4)
    private BigDecimal downPayment = BigDecimal.ZERO;

    @Column(name = "installment_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal installmentAmount;

    @Column(nullable = false)
    private String frequency = "MONTHLY";

    @Column(name = "first_due_date", nullable = false)
    private LocalDate firstDueDate;

    @Column(name = "number_of_installments", nullable = false)
    private Integer numberOfInstallments;

    @Column(name = "responsible_officer")
    private String responsibleOfficer;

    @Column(name = "approval_status", nullable = false)
    private String approvalStatus = "PENDING";

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private String status = "ACTIVE";
}