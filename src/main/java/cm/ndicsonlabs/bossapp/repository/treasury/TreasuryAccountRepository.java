// src/main/java/com/institution/finance/repository/TreasuryAccountRepository.java
package cm.ndicsonlabs.bossapp.repository.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreasuryAccountRepository extends JpaRepository<TreasuryAccount, UUID> {

    List<TreasuryAccount> findByActiveTrueOrderByCode();
}