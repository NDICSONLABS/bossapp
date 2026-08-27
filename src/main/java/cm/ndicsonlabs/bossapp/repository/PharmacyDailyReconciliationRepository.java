package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PharmacyDailyReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PharmacyDailyReconciliationRepository extends JpaRepository<PharmacyDailyReconciliation, UUID> {

    Optional<PharmacyDailyReconciliation> findByDepartmentIdAndReconciliationDate(
            UUID departmentId,
            LocalDate reconciliationDate
    );

    List<PharmacyDailyReconciliation> findTop500ByOrderByCreatedAtDesc();
}
