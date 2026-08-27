// src/main/java/com/institution/finance/repository/PurchaseOrderRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {

    List<PurchaseOrder> findByOrderByCreatedAtDesc();

    List<PurchaseOrder> findByStatusIn(Collection<String> statuses);
}