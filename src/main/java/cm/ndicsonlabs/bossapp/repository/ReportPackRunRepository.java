// src/main/java/com/institution/finance/repository/ReportPackRunRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ReportPackRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReportPackRunRepository extends JpaRepository<ReportPackRun, UUID> {
}