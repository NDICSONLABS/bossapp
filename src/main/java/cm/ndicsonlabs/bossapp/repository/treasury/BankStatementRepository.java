// src/main/java/com/institution/finance/repository/BankStatementRepository.java
package cm.ndicsonlabs.bossapp.repository.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankStatement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {

    List<BankStatement> findByOrderByCreatedAtDesc();

    List<BankStatement> findByTreasuryAccountIdOrderByStatementDateDesc(UUID treasuryAccountId);
}