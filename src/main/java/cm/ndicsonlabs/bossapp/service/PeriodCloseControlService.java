// src/main/java/com/institution/finance/service/PeriodCloseControlService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class PeriodCloseControlService {

    private final AccountingPeriodRepository periodRepository;
    private final FinancialStatementValidationService validationService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public PeriodCloseControlService(
            AccountingPeriodRepository periodRepository,
            FinancialStatementValidationService validationService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.periodRepository = periodRepository;
        this.validationService = validationService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public AccountingPeriod softClose(UUID periodId) {
        requireClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"OPEN".equals(period.getStatus())) {
            throw new IllegalStateException("Only open periods can be soft closed.");
        }

        validationService.validatePeriod(periodId);

        period.setStatus("SOFT_CLOSED");
        period.setClosedBy(currentUserService.username());
        period.setClosedAt(Instant.now());

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "SOFT_CLOSE",
                null,
                period.getStatus(),
                "Period soft closed"
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod hardClose(UUID periodId) {
        requireClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"SOFT_CLOSED".equals(period.getStatus())) {
            throw new IllegalStateException("Only soft closed periods can be hard closed.");
        }

        validationService.validatePeriod(periodId);

        period.setStatus("CLOSED");
        period.setClosedBy(currentUserService.username());
        period.setClosedAt(Instant.now());

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "HARD_CLOSE",
                null,
                period.getStatus(),
                "Period hard closed"
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod lock(UUID periodId) {
        requireClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if (!"CLOSED".equals(period.getStatus())) {
            throw new IllegalStateException("Only closed periods can be locked.");
        }

        period.setStatus("LOCKED");
        period.setClosedBy(currentUserService.username());
        period.setLockedDate(java.time.LocalDate.now());

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "LOCK",
                null,
                period.getStatus(),
                "Period locked"
        );

        return periodRepository.save(period);
    }

    @Transactional
    public AccountingPeriod reopen(UUID periodId, String reason) {
        requireClosePrivilege();

        AccountingPeriod period = getPeriod(periodId);

        if ("OPEN".equals(period.getStatus())) {
            throw new IllegalStateException("Period is already open.");
        }

        period.setStatus("OPEN");
        period.setReopenReason(reason);

        auditService.log(
                "ACCOUNTING_PERIOD",
                period.getId(),
                "REOPEN",
                null,
                period.getStatus(),
                reason
        );

        return periodRepository.save(period);
    }

    private AccountingPeriod getPeriod(UUID periodId) {
        return periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));
    }

    private void requireClosePrivilege() {
        if (!currentUserService.hasPrivilege("PERIOD_CLOSE")) {
            throw new AccessDeniedException("Current user does not have period close privilege.");
        }
    }
}