// src/main/java/com/institution/finance/repository/AccountingEntryRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
        select e.sourceType, e.sourceId, count(e)
        from AccountingEntry e
        where e.sourceType is not null
          and e.sourceId is not null
          and e.status = 'POSTED'
          and e.reversedByEntryId is null
        group by e.sourceType, e.sourceId
        having count(e) > 1
""")
    List<Object[]> findDuplicatePostedSources();

    List<AccountingEntry> findBySourceTypeAndSourceIdAndStatusOrderByCreatedAtAsc(
            String sourceType,
            UUID sourceId,
            String status
    );
}