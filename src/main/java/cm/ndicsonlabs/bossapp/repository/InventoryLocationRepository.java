// src/main/java/com/institution/finance/repository/InventoryLocationRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.InventoryLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryLocationRepository extends JpaRepository<InventoryLocation, UUID> {

    List<InventoryLocation> findByOrderByCode();
}