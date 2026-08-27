// src/main/java/com/institution/finance/repository/AccountingEntryRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, UUID> {

    boolean existsBySourceTypeAndSourceId(String sourceType, UUID sourceId);

    List<AccountingEntry> findByOrderByCreatedAtDesc();

    List<AccountingEntry> findByStatus(String status);

    List<AccountingEntry> findByEntryDateBetweenAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            String status
    );
}