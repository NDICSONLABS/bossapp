// src/main/java/com/institution/finance/repository/InventoryBalanceRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.InventoryBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryBalanceRepository extends JpaRepository<InventoryBalance, UUID> {

    Optional<InventoryBalance> findByItemIdAndLocationIdAndBatchId(
            UUID itemId,
            UUID locationId,
            UUID batchId
    );

    Optional<InventoryBalance> findByItemIdAndLocationIdAndBatchIsNull(
            UUID itemId,
            UUID locationId
    );

    List<InventoryBalance> findByLocationId(UUID locationId);

    long countByQuantityOnHandLessThan(java.math.BigDecimal quantity);
}