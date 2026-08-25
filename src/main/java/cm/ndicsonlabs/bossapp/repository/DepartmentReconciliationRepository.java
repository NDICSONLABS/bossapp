// src/main/java/com/institution/finance/repository/DepartmentReconciliationRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.DepartmentReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepartmentReconciliationRepository extends JpaRepository<DepartmentReconciliation, UUID> {
}