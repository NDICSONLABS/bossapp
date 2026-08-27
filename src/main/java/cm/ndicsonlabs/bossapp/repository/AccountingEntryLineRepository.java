// src/main/java/com/institution/finance/repository/AccountingEntryLineRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AccountingEntryLineRepository extends JpaRepository<AccountingEntryLine, UUID> {
    @Query("""
        select l
        from AccountingEntryLine l
        join l.entry e
        where l.accountCode.id = :accountCodeId
          and e.status = 'POSTED'
""")
    List<AccountingEntryLine> findPostedByAccountId(@Param("accountCodeId") UUID accountCodeId);
    List<AccountingEntryLine> findByEntryId(UUID entryId);
}