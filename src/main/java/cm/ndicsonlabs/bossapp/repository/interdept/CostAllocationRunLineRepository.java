// src/main/java/com/institution/finance/repository/CostAllocationRunLineRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRunLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CostAllocationRunLineRepository extends JpaRepository<CostAllocationRunLine, UUID> {

    List<CostAllocationRunLine> findByRunId(UUID runId);
}