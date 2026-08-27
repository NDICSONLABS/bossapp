// src/main/java/com/institution/finance/repository/StudentChargeRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.FeeSchedule;
import cm.ndicsonlabs.bossapp.domain.Student;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface StudentChargeRepository extends JpaRepository<StudentCharge, UUID> {
    List<StudentCharge> findByDepartmentAndChargeDateBetweenAndAccountingStatusIn(
            Department department,
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
    boolean existsByStudentAndFeeSchedule(Student student, FeeSchedule feeSchedule);
    List<StudentCharge> findByGlStatusIn(Collection<String> statuses);

    long countByChargeDateBetweenAndGlStatusIn(
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
}