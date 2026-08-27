// src/main/java/com/institution/finance/domain/ItemCategory.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "item_category")
public class ItemCategory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "category_type")
    private String categoryType;

    @Column(nullable = false)
    private boolean active = true;

    @Override
    public String toString() {
        return name;
    }
}