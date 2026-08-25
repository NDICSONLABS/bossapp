// src/main/java/com/institution/finance/repository/InsuranceProviderRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, UUID> {
}