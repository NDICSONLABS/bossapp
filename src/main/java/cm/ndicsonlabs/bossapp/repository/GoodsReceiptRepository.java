// src/main/java/com/institution/finance/repository/GoodsReceiptRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {

    Optional<GoodsReceipt> findFirstByPurchaseOrderIdOrderByCreatedAtDesc(UUID purchaseOrderId);
}