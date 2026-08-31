// src/main/java/com/institution/finance/domain/ReportPackRun.java
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
@Table(name = "report_pack_run")
public class ReportPackRun extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "period_id")
    private AccountingPeriod period;

    @Column(name = "report_code", nullable = false)
    private String reportCode;

    @Column(name = "output_format", nullable = false)
    private String outputFormat;

    @Column(name = "file_name")
    private String fileName;

    @Column(nullable = false)
    private String status = "SUCCESS";

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "generated_by")
    private String generatedBy;
}