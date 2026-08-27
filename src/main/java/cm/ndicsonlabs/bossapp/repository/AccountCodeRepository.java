// src/main/java/com/institution/finance/repository/AccountCodeRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountCodeRepository extends JpaRepository<AccountCode, UUID> {

    Optional<AccountCode> findByCode(String code);

    List<AccountCode> findByOrderByCode();
}