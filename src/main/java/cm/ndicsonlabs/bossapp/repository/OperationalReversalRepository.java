// src/main/java/com/institution/finance/repository/OperationalReversalRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.OperationalReversal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationalReversalRepository extends JpaRepository<OperationalReversal, UUID> {
}