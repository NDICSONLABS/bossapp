// src/main/java/com/institution/finance/domain/InventoryLocation.java
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
@Table(name = "inventory_location")
public class InventoryLocation extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "location_type")
    private String locationType;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return name;
    }
}