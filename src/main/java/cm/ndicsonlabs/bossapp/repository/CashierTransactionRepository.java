// src/main/java/com/institution/finance/repository/CashierTransactionRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.CashierTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CashierTransactionRepository extends JpaRepository<CashierTransaction, UUID> {

    List<CashierTransaction> findByCashierSessionId(UUID cashierSessionId);
}