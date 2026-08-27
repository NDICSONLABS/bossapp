// src/main/java/com/institution/finance/repository/GlReconciliationRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GlReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GlReconciliationRepository extends JpaRepository<GlReconciliation, UUID> {

    List<GlReconciliation> findTop500ByOrderByCreatedAtDesc();
}