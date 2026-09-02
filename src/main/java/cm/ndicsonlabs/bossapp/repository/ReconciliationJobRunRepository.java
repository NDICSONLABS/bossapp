// src/main/java/com/institution/finance/repository/ReconciliationJobRunRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ReconciliationJobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReconciliationJobRunRepository extends JpaRepository<ReconciliationJobRun, UUID> {
}