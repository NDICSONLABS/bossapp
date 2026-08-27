// src/main/java/com/institution/finance/repository/PurchaseRequestRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, UUID> {

    List<PurchaseRequest> findByOrderByCreatedAtDesc();
}