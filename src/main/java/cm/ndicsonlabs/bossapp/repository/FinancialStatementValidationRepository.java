package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.FinancialStatementValidation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancialStatementValidationRepository extends JpaRepository<FinancialStatementValidation, UUID> {

    void deleteByAccountingPeriodId(UUID accountingPeriodId);
}