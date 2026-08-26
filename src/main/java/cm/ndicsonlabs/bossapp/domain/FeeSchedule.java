// src/main/java/com/institution/finance/domain/FeeSchedule.java
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
@Table(name = "fee_schedule")
public class FeeSchedule extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private AcademicTerm term;

    @Column(name = "program_or_class")
    private String programOrClass;

    @Column(name = "student_category")
    private String studentCategory;

    @Column(name = "fee_type", nullable = false)
    private String feeType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String currency;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber = 1;

    @Column(nullable = false)
    private boolean mandatory = true;

    @Column(nullable = false)
    private boolean active = true;
}