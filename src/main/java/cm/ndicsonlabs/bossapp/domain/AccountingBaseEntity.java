package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.time.Instant;

@MappedSuperclass
public abstract class AccountingBaseEntity extends BaseEntity {
    @Column(name = "accounting_status", nullable = false)
    private String accountingStatus = "NOT_SUBMITTED";

    @Column(name = "gl_status", nullable = false)
    private String glStatus = "NOT_POSTED";

    @Column(name = "gl_error", columnDefinition = "TEXT")
    private String glError;

    @Column(name = "gl_posted_at")
    private Instant glPostedAt;

    public String getAccountingStatus() {
        return accountingStatus;
    }

    public void setAccountingStatus(String accountingStatus) {
        this.accountingStatus = accountingStatus;
    }

    public String getGlStatus() {
        return glStatus;
    }

    public void setGlStatus(String glStatus) {
        this.glStatus = glStatus;
    }

    public String getGlError() {
        return glError;
    }

    public void setGlError(String glError) {
        this.glError = glError;
    }

    public Instant getGlPostedAt() {
        return glPostedAt;
    }

    public void setGlPostedAt(Instant glPostedAt) {
        this.glPostedAt = glPostedAt;
    }
}
