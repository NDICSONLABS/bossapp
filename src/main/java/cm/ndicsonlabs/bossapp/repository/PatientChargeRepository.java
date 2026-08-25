// src/main/java/com/institution/finance/repository/PatientChargeRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PatientChargeRepository extends JpaRepository<PatientCharge, UUID> {
    List<PatientCharge> findByDepartmentAndChargeDateBetweenAndAccountingStatusIn(
            Department department,
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
}