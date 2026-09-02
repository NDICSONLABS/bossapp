// src/main/java/com/institution/finance/service/FinancialStatementValidationService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.FinancialStatementValidation;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.FinancialStatementValidationRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class FinancialStatementValidationService {

    private final AccountingPeriodRepository periodRepository;
    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final FinancialStatementValidationRepository validationRepository;
    private final CurrentUserService currentUserService;

    public FinancialStatementValidationService(
            AccountingPeriodRepository periodRepository,
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            FinancialStatementValidationRepository validationRepository,
            CurrentUserService currentUserService
    ) {
        this.periodRepository = periodRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.validationRepository = validationRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public void validatePeriod(UUID periodId) {
        if (!currentUserService.hasPrivilege("FINANCIAL_VALIDATION")) {
            throw new AccessDeniedException("Current user cannot run financial validations.");
        }

        AccountingPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting period not found"));

        validationRepository.deleteByAccountingPeriodId(periodId);

        validateTrialBalance(period);
        validateBalanceSheetEquation(period);
    }

    private void validateTrialBalance(AccountingPeriod period) {
        List<AccountingEntry> entries = entryRepository.findByEntryDateBetweenAndStatus(
                period.getStartDate(),
                period.getEndDate(),
                "POSTED"
        );

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                totalDebit = totalDebit.add(nullSafe(line.getDebit()));
                totalCredit = totalCredit.add(nullSafe(line.getCredit()));
            }
        }

        boolean balanced = totalDebit.compareTo(totalCredit) == 0;

        saveValidation(
                period,
                "TRIAL_BALANCE_BALANCED",
                balanced ? "PASS" : "FAIL",
                "Total debit: " + totalDebit + ", total credit: " + totalCredit,
                totalDebit.subtract(totalCredit)
        );

        if (!balanced) {
            throw new IllegalStateException("Trial balance validation failed.");
        }
    }

    private void validateBalanceSheetEquation(AccountingPeriod period) {
        List<AccountingEntry> entries = entryRepository.findByEntryDateBetweenAndStatus(
                period.getStartDate(),
                period.getEndDate(),
                "POSTED"
        );

        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;
        BigDecimal equity = BigDecimal.ZERO;
        BigDecimal revenue = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (AccountingEntry entry : entries) {
            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                AccountCode account = line.getAccountCode();

                if (account == null) {
                    continue;
                }

                BigDecimal debit = nullSafe(line.getDebit());
                BigDecimal credit = nullSafe(line.getCredit());

                switch (account.getAccountType()) {
                    case "ASSET" -> assets = assets.add(debit.subtract(credit));
                    case "LIABILITY" -> liabilities = liabilities.add(credit.subtract(debit));
                    case "EQUITY" -> equity = equity.add(credit.subtract(debit));
                    case "REVENUE" -> revenue = revenue.add(credit.subtract(debit));
                    case "EXPENSE" -> expense = expense.add(debit.subtract(credit));
                    default -> {
                    }
                }
            }
        }

        BigDecimal netIncome = revenue.subtract(expense);
        BigDecimal totalEquity = equity.add(netIncome);
        BigDecimal liabilitiesAndEquity = liabilities.add(totalEquity);

        boolean balanced = assets.compareTo(liabilitiesAndEquity) == 0;

        saveValidation(
                period,
                "BALANCE_SHEET_EQUATION",
                balanced ? "PASS" : "FAIL",
                "Assets: " + assets + ", Liabilities and Equity: " + liabilitiesAndEquity,
                assets.subtract(liabilitiesAndEquity)
        );

        if (!balanced) {
            throw new IllegalStateException("Balance sheet equation validation failed.");
        }
    }

    private void saveValidation(
            AccountingPeriod period,
            String code,
            String status,
            String message,
            BigDecimal value
    ) {
        FinancialStatementValidation validation = new FinancialStatementValidation();
        validation.setAccountingPeriod(period);
        validation.setValidationCode(code);
        validation.setStatus(status);
        validation.setMessage(message);
        validation.setNumericValue(value);

        validationRepository.save(validation);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}