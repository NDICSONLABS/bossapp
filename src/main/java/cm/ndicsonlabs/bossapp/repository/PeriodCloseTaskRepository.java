// src/main/java/com/institution/finance/repository/PeriodCloseTaskRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PeriodCloseTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodCloseTaskRepository extends JpaRepository<PeriodCloseTask, UUID> {

    List<PeriodCloseTask> findByPeriodIdOrderByCreatedAtAsc(UUID periodId);

    Optional<PeriodCloseTask> findByPeriodIdAndTaskCode(UUID periodId, String taskCode);
}