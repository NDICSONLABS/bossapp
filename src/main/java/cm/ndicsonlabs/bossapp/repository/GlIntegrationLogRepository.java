// src/main/java/com/institution/finance/repository/GlIntegrationLogRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GlIntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GlIntegrationLogRepository extends JpaRepository<GlIntegrationLog, UUID> {

    List<GlIntegrationLog> findTop500ByOrderByCreatedAtDesc();
}