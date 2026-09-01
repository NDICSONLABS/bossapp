// src/main/java/com/institution/finance/repository/InternalSettlementRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.InternalSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InternalSettlementRepository extends JpaRepository<InternalSettlement, UUID> {
}