// src/main/java/com/institution/finance/repository/CashFlowAdjustmentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.CashFlowAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CashFlowAdjustmentRepository extends JpaRepository<CashFlowAdjustment, UUID> {
    List<CashFlowAdjustment> findByAdjustmentDateBetween(LocalDate start, LocalDate end);
}