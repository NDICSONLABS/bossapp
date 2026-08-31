// src/main/java/com/institution/finance/repository/BankReconciliationRepository.java
package cm.ndicsonlabs.bossapp.repository.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankReconciliationRepository extends JpaRepository<BankReconciliation, UUID> {

    List<BankReconciliation> findByOrderByCreatedAtDesc();

    Optional<BankReconciliation> findFirstByBankStatementIdOrderByCreatedAtDesc(UUID bankStatementId);
}