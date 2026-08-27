// src/main/java/com/institution/finance/service/FinancialStatementService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.dto.StatementLine;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FinancialStatementService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;

    public FinancialStatementService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
    }

    public List<StatementLine> statementOfFinancialPosition(LocalDate asOf) {
        Map<String, AccountTotals> totals = collectTotals(null, asOf);

        List<StatementLine> lines = new ArrayList<>();

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (AccountTotals total : totals.values()) {
            AccountCode account = total.account;

            switch (account.getAccountType()) {
                case "ASSET" -> {
                    BigDecimal amount = total.debit.subtract(total.credit);
                    lines.add(new StatementLine("Assets", account.getCode() + " - " + account.getName(), amount));
                    totalAssets = totalAssets.add(amount);
                }

                case "LIABILITY" -> {
                    BigDecimal amount = total.credit.subtract(total.debit);
                    lines.add(new StatementLine("Liabilities", account.getCode() + " - " + account.getName(), amount));
                    totalLiabilities = totalLiabilities.add(amount);
                }

                case "EQUITY" -> {
                    BigDecimal amount = total.credit.subtract(total.debit);
                    lines.add(new StatementLine("Equity", account.getCode() + " - " + account.getName(), amount));
                    totalEquity = totalEquity.add(amount);
                }

                case "REVENUE" -> totalRevenue = totalRevenue.add(total.credit.subtract(total.debit));

                case "EXPENSE" -> totalExpense = totalExpense.add(total.debit.subtract(total.credit));

                default -> {
                }
            }
        }

        BigDecimal currentSurplus = totalRevenue.subtract(totalExpense);

        lines.add(new StatementLine("Equity", "Current Period Surplus", currentSurplus));
        totalEquity = totalEquity.add(currentSurplus);

        lines.add(new StatementLine("Totals", "Total Assets", totalAssets));
        lines.add(new StatementLine("Totals", "Total Liabilities", totalLiabilities));
        lines.add(new StatementLine("Totals", "Total Equity", totalEquity));
        lines.add(new StatementLine("Totals", "Total Liabilities and Equity", totalLiabilities.add(totalEquity)));

        return lines;
    }

    public List<StatementLine> statementOfActivity(LocalDate from, LocalDate to) {
        Map<String, AccountTotals> totals = collectTotals(from, to);

        List<StatementLine> lines = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (AccountTotals total : totals.values()) {
            AccountCode account = total.account;

            if ("REVENUE".equals(account.getAccountType())) {
                BigDecimal amount = total.credit.subtract(total.debit);
                lines.add(new StatementLine("Revenue", account.getCode() + " - " + account.getName(), amount));
                totalRevenue = totalRevenue.add(amount);
            }

            if ("EXPENSE".equals(account.getAccountType())) {
                BigDecimal amount = total.debit.subtract(total.credit);
                lines.add(new StatementLine("Expenses", account.getCode() + " - " + account.getName(), amount));
                totalExpense = totalExpense.add(amount);
            }
        }

        lines.add(new StatementLine("Totals", "Total Revenue", totalRevenue));
        lines.add(new StatementLine("Totals", "Total Expenses", totalExpense));
        lines.add(new StatementLine("Totals", "Surplus / Deficit", totalRevenue.subtract(totalExpense)));

        return lines;
    }

    private Map<String, AccountTotals> collectTotals(LocalDate from, LocalDate to) {
        List<AccountingEntry> entries = entryRepository.findByStatus("POSTED");

        Map<String, AccountTotals> totals = new LinkedHashMap<>();

        for (AccountingEntry entry : entries) {
            if (!isWithinRange(entry.getEntryDate(), from, to)) {
                continue;
            }

            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                AccountCode account = line.getAccountCode();

                AccountTotals total = totals.computeIfAbsent(
                        account.getCode(),
                        key -> new AccountTotals(account)
                );

                total.debit = total.debit.add(nullSafe(line.getDebit()));
                total.credit = total.credit.add(nullSafe(line.getCredit()));
            }
        }

        return totals;
    }

    private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        if (date == null) {
            return false;
        }

        if (from != null && date.isBefore(from)) {
            return false;
        }

        return to == null || !date.isAfter(to);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static class AccountTotals {
        private final AccountCode account;
        private BigDecimal debit = BigDecimal.ZERO;
        private BigDecimal credit = BigDecimal.ZERO;

        private AccountTotals(AccountCode account) {
            this.account = account;
        }
    }
}