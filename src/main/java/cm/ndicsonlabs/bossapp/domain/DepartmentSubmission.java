// src/main/java/com/institution/finance/domain/DepartmentSubmission.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "department_submission")
public class DepartmentSubmission extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private AccountingPeriod period;

    @Column(name = "opening_ap_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingApBalance = BigDecimal.ZERO;

    @Column(name = "new_ap_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal newApAmount = BigDecimal.ZERO;

    @Column(name = "ap_payments", nullable = false, precision = 19, scale = 4)
    private BigDecimal apPayments = BigDecimal.ZERO;

    @Column(name = "closing_ap_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingApBalance = BigDecimal.ZERO;

    @Column(name = "opening_ar_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal openingArBalance = BigDecimal.ZERO;

    @Column(name = "new_ar_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal newArAmount = BigDecimal.ZERO;

    @Column(name = "ar_collections", nullable = false, precision = 19, scale = 4)
    private BigDecimal arCollections = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal adjustments = BigDecimal.ZERO;

    @Column(name = "closing_ar_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal closingArBalance = BigDecimal.ZERO;

    @Column(name = "transaction_count", nullable = false)
    private Integer transactionCount = 0;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "submitted_by")
    private String submittedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "department_approved_by")
    private String departmentApprovedBy;

    @Column(name = "department_approved_at")
    private Instant departmentApprovedAt;

    @Column(name = "central_reviewed_by")
    private String centralReviewedBy;

    @Column(name = "central_reviewed_at")
    private Instant centralReviewedAt;

    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;

    @Column(nullable = false)
    private String status = "DRAFT";

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public AccountingPeriod getPeriod() {
        return period;
    }

    public void setPeriod(AccountingPeriod period) {
        this.period = period;
    }

    public BigDecimal getOpeningApBalance() {
        return openingApBalance;
    }

    public void setOpeningApBalance(BigDecimal openingApBalance) {
        this.openingApBalance = openingApBalance;
    }

    public BigDecimal getNewApAmount() {
        return newApAmount;
    }

    public void setNewApAmount(BigDecimal newApAmount) {
        this.newApAmount = newApAmount;
    }

    public BigDecimal getApPayments() {
        return apPayments;
    }

    public void setApPayments(BigDecimal apPayments) {
        this.apPayments = apPayments;
    }

    public BigDecimal getClosingApBalance() {
        return closingApBalance;
    }

    public void setClosingApBalance(BigDecimal closingApBalance) {
        this.closingApBalance = closingApBalance;
    }

    public BigDecimal getOpeningArBalance() {
        return openingArBalance;
    }

    public void setOpeningArBalance(BigDecimal openingArBalance) {
        this.openingArBalance = openingArBalance;
    }

    public BigDecimal getNewArAmount() {
        return newArAmount;
    }

    public void setNewArAmount(BigDecimal newArAmount) {
        this.newArAmount = newArAmount;
    }

    public BigDecimal getArCollections() {
        return arCollections;
    }

    public void setArCollections(BigDecimal arCollections) {
        this.arCollections = arCollections;
    }

    public BigDecimal getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(BigDecimal adjustments) {
        this.adjustments = adjustments;
    }

    public BigDecimal getClosingArBalance() {
        return closingArBalance;
    }

    public void setClosingArBalance(BigDecimal closingArBalance) {
        this.closingArBalance = closingArBalance;
    }

    public Integer getTransactionCount() {
        return transactionCount;
    }

    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(String submittedBy) {
        this.submittedBy = submittedBy;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getDepartmentApprovedBy() {
        return departmentApprovedBy;
    }

    public void setDepartmentApprovedBy(String departmentApprovedBy) {
        this.departmentApprovedBy = departmentApprovedBy;
    }

    public Instant getDepartmentApprovedAt() {
        return departmentApprovedAt;
    }

    public void setDepartmentApprovedAt(Instant departmentApprovedAt) {
        this.departmentApprovedAt = departmentApprovedAt;
    }

    public String getCentralReviewedBy() {
        return centralReviewedBy;
    }

    public void setCentralReviewedBy(String centralReviewedBy) {
        this.centralReviewedBy = centralReviewedBy;
    }

    public Instant getCentralReviewedAt() {
        return centralReviewedAt;
    }

    public void setCentralReviewedAt(Instant centralReviewedAt) {
        this.centralReviewedAt = centralReviewedAt;
    }

    public String getReviewComments() {
        return reviewComments;
    }

    public void setReviewComments(String reviewComments) {
        this.reviewComments = reviewComments;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}