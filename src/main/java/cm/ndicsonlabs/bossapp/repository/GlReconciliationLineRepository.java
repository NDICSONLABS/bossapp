// src/main/java/com/institution/finance/repository/GlReconciliationLineRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GlReconciliationLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GlReconciliationLineRepository extends JpaRepository<GlReconciliationLine, UUID> {

    List<GlReconciliationLine> findByGlReconciliationIdOrderByCreatedAtAsc(UUID glReconciliationId);
}