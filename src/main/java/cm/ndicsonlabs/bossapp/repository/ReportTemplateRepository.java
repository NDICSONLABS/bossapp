// src/main/java/com/institution/finance/repository/ReportTemplateRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, UUID> {
    Optional<ReportTemplate> findByCode(String code);

    List<ReportTemplate> findByActiveTrueOrderByCode();
}