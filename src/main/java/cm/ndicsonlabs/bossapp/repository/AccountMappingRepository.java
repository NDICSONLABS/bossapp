// src/main/java/com/institution/finance/repository/AccountMappingRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountMappingRepository extends JpaRepository<AccountMapping, UUID> {

    Optional<AccountMapping> findByMappingType(String mappingType);

    List<AccountMapping> findAllByOrderByMappingType();
}