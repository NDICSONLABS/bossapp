// src/main/java/com/institution/finance/repository/CostCenterRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.CostCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CostCenterRepository extends JpaRepository<CostCenter, UUID> {
}