// src/main/java/com/institution/finance/domain/InventoryItem.java
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
@Table(name = "inventory_item")
public class InventoryItem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private ItemCategory category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unit_of_measure_id", nullable = false)
    private UnitOfMeasure unitOfMeasure;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "batch_controlled", nullable = false)
    private boolean batchControlled = false;

    @Column(name = "expiry_controlled", nullable = false)
    private boolean expiryControlled = false;

    @Column(name = "standard_cost", precision = 19, scale = 4)
    private BigDecimal standardCost;

    @Column(name = "sale_price", precision = 19, scale = 4)
    private BigDecimal salePrice;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return code + " - " + name;
    }
}