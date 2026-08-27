// src/main/java/com/institution/finance/repository/ItemCategoryRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ItemCategoryRepository extends JpaRepository<ItemCategory, UUID> {
}