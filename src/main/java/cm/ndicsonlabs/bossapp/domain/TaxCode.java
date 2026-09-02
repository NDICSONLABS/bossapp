// src/main/java/com/institution/finance/domain/TaxCode.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "tax_code")
public class TaxCode extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 9, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "tax_type", nullable = false)
    private String taxType = "EXCLUSIVE";

    @ManyToOne
    @JoinColumn(name = "sales_account_code_id")
    private AccountCode salesAccountCode;

    @ManyToOne
    @JoinColumn(name = "purchase_account_code_id")
    private AccountCode purchaseAccountCode;

    @Column(nullable = false)
    private boolean active = true;
}