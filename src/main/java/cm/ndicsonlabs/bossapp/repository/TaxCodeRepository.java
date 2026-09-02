// src/main/java/com/institution/finance/repository/TaxCodeRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.TaxCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaxCodeRepository extends JpaRepository<TaxCode, UUID> {
}