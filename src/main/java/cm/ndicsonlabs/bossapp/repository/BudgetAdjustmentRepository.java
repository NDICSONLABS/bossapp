// src/main/java/com/institution/finance/repository/BudgetAdjustmentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.BudgetAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BudgetAdjustmentRepository extends JpaRepository<BudgetAdjustment, UUID> {
}