// src/main/java/com/institution/finance/repository/CostAllocationRunRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CostAllocationRunRepository extends JpaRepository<CostAllocationRun, UUID> {

    boolean existsByRuleIdAndPeriodYearAndPeriodMonth(UUID ruleId, Integer periodYear, Integer periodMonth);
}