// src/main/java/com/institution/finance/repository/CashierSessionRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.CashierSession;
import cm.ndicsonlabs.bossapp.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CashierSessionRepository extends JpaRepository<CashierSession, UUID> {

    boolean existsByDepartmentAndSessionDate(Department department, LocalDate sessionDate);

    List<CashierSession> findByOrderByCreatedAtDesc();
}