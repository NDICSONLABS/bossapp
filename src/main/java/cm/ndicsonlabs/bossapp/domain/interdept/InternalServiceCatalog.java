// src/main/java/com/institution/finance/domain/InternalServiceCatalog.java
package cm.ndicsonlabs.bossapp.domain.interdept;

import cm.ndicsonlabs.bossapp.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "internal_service_catalog")
public class InternalServiceCatalog extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_price", precision = 19, scale = 4)
    private BigDecimal defaultPrice;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return code + " - " + name;
    }
}