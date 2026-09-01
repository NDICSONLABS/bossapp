// src/main/java/com/institution/finance/repository/CostAllocationRuleTargetRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRuleTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CostAllocationRuleTargetRepository extends JpaRepository<CostAllocationRuleTarget, UUID> {

    List<CostAllocationRuleTarget> findByRuleId(UUID ruleId);
}