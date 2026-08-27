// src/main/java/com/institution/finance/domain/StudentCharge.java
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
@Table(name = "student_charge")
public class StudentCharge extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne
    @JoinColumn(name = "academic_year_id")
    private AcademicYear academicYear;

    @ManyToOne
    @JoinColumn(name = "term_id")
    private AcademicTerm term;

    @ManyToOne
    @JoinColumn(name = "fee_schedule_id")
    private FeeSchedule feeSchedule;

    @Column(name = "service_category")
    private String serviceCategory;

    @Column(name = "charge_date")
    private LocalDate chargeDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(name = "original_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal originalAmount;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "scholarship_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal scholarshipAmount = BigDecimal.ZERO;

    @Column(name = "waiver_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal waiverAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal netAmount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status = "POSTED";

    @Column(name = "accounting_status", nullable = false)
    private String accountingStatus = "NOT_SUBMITTED";

    @Column(name = "gl_status", nullable = false)
    private String glStatus = "NOT_POSTED";

    @Column(name = "gl_error", columnDefinition = "TEXT")
    private String glError;

    @Column(name = "gl_posted_at")
    private Instant glPostedAt;

    public BigDecimal getRemainingAmount() {
        BigDecimal target = netAmount != null ? netAmount : amount;
        return nullSafe(target).subtract(nullSafe(paidAmount));
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
//package cm.ndicsonlabs.bossapp.domain;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.Table;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "student_charge")
//public class StudentCharge extends AccountingBaseEntity {
//
//    @ManyToOne(optional = false)
//    @JoinColumn(name = "student_id", nullable = false)
//    private Student student;
//
//    @ManyToOne(optional = false)
//    @JoinColumn(name = "department_id", nullable = false)
//    private Department department;
//
//    @Column(name = "charge_date")
//    private LocalDate chargeDate;
//
//    @Column(name = "due_date")
//    private LocalDate dueDate;
//
//    @Column(nullable = false, precision = 19, scale = 4)
//    private BigDecimal amount;
//
//    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 4)
//    private BigDecimal paidAmount = BigDecimal.ZERO;
//
//    @Column(nullable = false)
//    private String status = "POSTED";
//
//    public Student getStudent() {
//        return student;
//    }
//
//    public void setStudent(Student student) {
//        this.student = student;
//    }
//
//    public Department getDepartment() {
//        return department;
//    }
//
//    public void setDepartment(Department department) {
//        this.department = department;
//    }
//
//    public LocalDate getChargeDate() {
//        return chargeDate;
//    }
//
//    public void setChargeDate(LocalDate chargeDate) {
//        this.chargeDate = chargeDate;
//    }
//
//    public LocalDate getDueDate() {
//        return dueDate;
//    }
//
//    public void setDueDate(LocalDate dueDate) {
//        this.dueDate = dueDate;
//    }
//
//    public BigDecimal getAmount() {
//        return amount;
//    }
//
//    public void setAmount(BigDecimal amount) {
//        this.amount = amount;
//    }
//
//    public BigDecimal getPaidAmount() {
//        return paidAmount;
//    }
//
//    public void setPaidAmount(BigDecimal paidAmount) {
//        this.paidAmount = paidAmount;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public BigDecimal getRemainingAmount() {
//        return amount.subtract(paidAmount);
//    }
//}
