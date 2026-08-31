// src/main/java/com/institution/finance/repository/BankStatementLineRepository.java
package cm.ndicsonlabs.bossapp.repository.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankStatementLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BankStatementLineRepository extends JpaRepository<BankStatementLine, UUID> {

    List<BankStatementLine> findByBankStatementIdOrderByLineNumberAsc(UUID bankStatementId);

    List<BankStatementLine> findByBankStatementIdAndStatusOrderByLineNumberAsc(
            UUID bankStatementId,
            String status
    );

    long countByBankStatementIdAndStatus(UUID bankStatementId, String status);
}