// src/main/java/com/institution/finance/repository/GrantAwardRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GrantAward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GrantAwardRepository extends JpaRepository<GrantAward, UUID> {

    List<GrantAward> findByOrderByCode();
}