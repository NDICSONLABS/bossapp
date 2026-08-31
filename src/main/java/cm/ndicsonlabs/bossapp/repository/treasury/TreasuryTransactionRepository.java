// src/main/java/com/institution/finance/repository/TreasuryTransactionRepository.java
package cm.ndicsonlabs.bossapp.repository.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TreasuryTransactionRepository extends JpaRepository<TreasuryTransaction, UUID> {

    boolean existsBySourceTypeAndSourceId(String sourceType, UUID sourceId);

    List<TreasuryTransaction> findByTreasuryAccountIdOrderByTransactionDateDesc(UUID treasuryAccountId);

    List<TreasuryTransaction> findByTreasuryAccountIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            UUID treasuryAccountId,
            LocalDate start,
            LocalDate end
    );

    List<TreasuryTransaction> findByTreasuryAccountIdAndTransactionDateLessThanEqual(
            UUID treasuryAccountId,
            LocalDate date
    );

    List<TreasuryTransaction> findByTreasuryAccountIdAndStatusAndTransactionDateLessThanEqualOrderByTransactionDateAsc(
            UUID treasuryAccountId,
            String status,
            LocalDate date
    );
}