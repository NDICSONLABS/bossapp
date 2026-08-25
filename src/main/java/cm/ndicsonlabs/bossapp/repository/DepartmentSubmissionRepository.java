// src/main/java/com/institution/finance/repository/DepartmentSubmissionRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.DepartmentSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DepartmentSubmissionRepository extends JpaRepository<DepartmentSubmission, UUID> {

    List<DepartmentSubmission> findByOrderByCreatedAtDesc();

    boolean existsByPeriodAndStatusIn(AccountingPeriod period, Collection<String> statuses);

    boolean existsByPeriodAndDepartmentAndStatusIn(
            AccountingPeriod period,
            Department department,
            Collection<String> statuses
    );

    long countByStatus(String status);
}