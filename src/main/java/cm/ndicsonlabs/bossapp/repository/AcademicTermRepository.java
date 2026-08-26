// src/main/java/com/institution/finance/repository/AcademicTermRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.AcademicTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcademicTermRepository extends JpaRepository<AcademicTerm, UUID> {
}