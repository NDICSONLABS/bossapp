// src/main/java/com/institution/finance/repository/PeriodCloseValidationRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PeriodCloseValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PeriodCloseValidationRepository extends JpaRepository<PeriodCloseValidation, UUID> {

    List<PeriodCloseValidation> findByPeriodIdOrderByCreatedAtDesc(UUID periodId);

    Optional<PeriodCloseValidation> findTopByPeriodIdAndValidationCodeOrderByCreatedAtDesc(
            UUID periodId,
            String validationCode
    );
}