package cm.ndicsonlabs.bossapp.repository;


import cm.ndicsonlabs.bossapp.domain.SupplierCreditControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SupplierCreditControlRepository extends JpaRepository<SupplierCreditControl, UUID> {

    Optional<SupplierCreditControl> findBySupplierId(UUID supplierId);
}