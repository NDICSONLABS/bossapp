// src/main/java/com/institution/finance/repository/InventoryItemRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

    List<InventoryItem> findByOrderByCode();
}