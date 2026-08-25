// src/main/java/com/institution/finance/domain/ReportRun.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_run")
public class ReportRun extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "report_template_id", nullable = false)
    private ReportTemplate reportTemplate;

    @Column(name = "run_by_username")
    private String runByUsername;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(name = "output_format")
    private String outputFormat;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;

    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void setReportTemplate(ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    public String getRunByUsername() {
        return runByUsername;
    }

    public void setRunByUsername(String runByUsername) {
        this.runByUsername = runByUsername;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}