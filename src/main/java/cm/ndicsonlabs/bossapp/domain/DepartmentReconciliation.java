// src/main/java/com/institution/finance/domain/DepartmentReconciliation.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "department_reconciliation")
public class DepartmentReconciliation extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private DepartmentSubmission submission;

    @Column(nullable = false)
    private String description;

    @Column(name = "expected_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedAmount = BigDecimal.ZERO;

    @Column(name = "actual_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal actualAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(nullable = false)
    private String status = "OPEN";

    public DepartmentSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(DepartmentSubmission submission) {
        this.submission = submission;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getExpectedAmount() {
        return expectedAmount;
    }

    public void setExpectedAmount(BigDecimal expectedAmount) {
        this.expectedAmount = expectedAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public BigDecimal getVariance() {
        return variance;
    }

    public void setVariance(BigDecimal variance) {
        this.variance = variance;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}