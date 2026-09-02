// src/main/java/com/institution/finance/service/PeriodValidationService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PeriodValidationService {

    private final AccountingPeriodRepository periodRepository;
    private final CurrentUserService currentUserService;

    public PeriodValidationService(
            AccountingPeriodRepository periodRepository,
            CurrentUserService currentUserService
    ) {
        this.periodRepository = periodRepository;
        this.currentUserService = currentUserService;
    }

    public AccountingPeriod getOpenPeriodForPosting(LocalDate date) {
        AccountingPeriod period = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .orElseThrow(() -> new IllegalStateException("No accounting period found for date: " + date));

        ensurePostable(period);

        return period;
    }

    public void ensurePostable(AccountingPeriod period) {
        if (period == null) {
            throw new IllegalStateException("Accounting period is required.");
        }

        String status = period.getStatus();

        if ("LOCKED".equals(status) || "CLOSED".equals(status)) {
            throw new IllegalStateException(
                    "Accounting period " + period.getFiscalYear() + " P" + period.getPeriodNumber() +
                    " is closed or locked and cannot receive postings."
            );
        }

        if ("SOFT_CLOSED".equals(status) &&
                !currentUserService.hasPrivilege("PERIOD_SOFT_CLOSE_OVERRIDE")) {
            throw new AccessDeniedException(
                    "Accounting period is soft closed. Override privilege is required for posting."
            );
        }
    }
}