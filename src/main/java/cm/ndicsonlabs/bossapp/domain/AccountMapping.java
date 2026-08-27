// src/main/java/com/institution/finance/domain/AccountMapping.java
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
@Table(name = "account_mapping")
public class AccountMapping extends BaseEntity {

    @Column(name = "mapping_type", nullable = false, unique = true)
    private String mappingType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_code_id", nullable = false)
    private AccountCode accountCode;

    @Column(nullable = false)
    private boolean active = true;
}