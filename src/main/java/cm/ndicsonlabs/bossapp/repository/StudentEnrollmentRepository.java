// src/main/java/com/institution/finance/repository/StudentEnrollmentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AcademicTerm;
import cm.ndicsonlabs.bossapp.domain.AcademicYear;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, UUID> {

    List<StudentEnrollment> findByDepartmentAndAcademicYearAndStatus(
            Department department,
            AcademicYear academicYear,
            String status
    );

    List<StudentEnrollment> findByDepartmentAndAcademicYearAndTermAndStatus(
            Department department,
            AcademicYear academicYear,
            AcademicTerm term,
            String status
    );
}