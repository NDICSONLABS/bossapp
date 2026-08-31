// src/main/java/com/institution/finance/domain/PeriodCloseTask.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "period_close_task")
public class PeriodCloseTask extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private AccountingPeriod period;

    @Column(name = "task_code", nullable = false)
    private String taskCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean required = true;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "completed_by")
    private String completedBy;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}