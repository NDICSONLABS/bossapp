// src/main/java/com/institution/finance/repository/ReportRunRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ReportRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportRunRepository extends JpaRepository<ReportRun, UUID> {
}