// src/main/java/com/institution/finance/repository/PurchaseOrderLineRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, UUID> {

    List<PurchaseOrderLine> findByPurchaseOrderId(UUID purchaseOrderId);
}