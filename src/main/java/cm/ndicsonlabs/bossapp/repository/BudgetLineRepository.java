// src/main/java/com/institution/finance/repository/BudgetLineRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.BudgetLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetLineRepository extends JpaRepository<BudgetLine, UUID> {

    List<BudgetLine> findByBudgetHeaderIdOrderByCreatedAtAsc(UUID budgetHeaderId);
    void deleteByBudgetHeaderId(UUID budgetHeaderId);
}