// src/main/java/com/institution/finance/repository/FundRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Fund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FundRepository extends JpaRepository<Fund, UUID> {

    List<Fund> findByOrderByCode();
}