// src/main/java/com/institution/finance/repository/BudgetHeaderRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.BudgetHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetHeaderRepository extends JpaRepository<BudgetHeader, UUID> {

    List<BudgetHeader> findByOrderByCreatedAtDesc();
}