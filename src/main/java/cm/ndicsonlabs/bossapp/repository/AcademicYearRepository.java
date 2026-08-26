// src/main/java/com/institution/finance/repository/AcademicYearRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
}