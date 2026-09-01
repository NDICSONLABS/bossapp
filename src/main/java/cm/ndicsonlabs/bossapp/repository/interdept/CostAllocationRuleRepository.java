// src/main/java/com/institution/finance/repository/CostAllocationRuleRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CostAllocationRuleRepository extends JpaRepository<CostAllocationRule, UUID> {

    List<CostAllocationRule> findByActiveTrueOrderByName();
}