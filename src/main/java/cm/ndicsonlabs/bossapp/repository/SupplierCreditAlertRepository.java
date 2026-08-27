package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.SupplierCreditAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SupplierCreditAlertRepository extends JpaRepository<SupplierCreditAlert, UUID> {

    List<SupplierCreditAlert> findTop500ByOrderByCreatedAtDesc();

    boolean existsBySupplierIdAndAlertTypeAndSourceId(
            UUID supplierId,
            String alertType,
            UUID sourceId
    );
}