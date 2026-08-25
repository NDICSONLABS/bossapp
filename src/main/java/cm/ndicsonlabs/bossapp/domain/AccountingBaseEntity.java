package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AccountingBaseEntity extends BaseEntity {
    @Column(name = "accounting_status", nullable = false)
    private String accountingStatus = "NOT_SUBMITTED";

    public String getAccountingStatus() {
        return accountingStatus;
    }

    public void setAccountingStatus(String accountingStatus) {
        this.accountingStatus = accountingStatus;
    }
}
