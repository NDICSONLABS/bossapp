package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.SupplierBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierBatchRepository extends JpaRepository<SupplierBatch, UUID> {

    List<SupplierBatch> findByOrderByCreatedAtDesc();

    List<SupplierBatch> findByExpiryDateLessThanEqualAndStatus(LocalDate expiryDate, String status);

    Optional<SupplierBatch> findByBatchNumberAndItemId(String batchNumber, UUID itemId);
}