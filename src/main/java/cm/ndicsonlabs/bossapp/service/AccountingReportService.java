// src/main/java/com/institution/finance/service/AccountingReportService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.dto.LedgerLine;
import cm.ndicsonlabs.bossapp.dto.TrialBalanceLine;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AccountingReportService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;

    public AccountingReportService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
    }

    public List<TrialBalanceLine> trialBalance(LocalDate from, LocalDate to) {
        List<AccountingEntry> entries = loadPostedEntries(from, to);

        Map<String, Totals> totals = new LinkedHashMap<>();

        for (AccountingEntry entry : entries) {
            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                AccountCode account = line.getAccountCode();

                Totals total = totals.computeIfAbsent(
                        account.getCode(),
                        key -> new Totals(account)
                );

                total.debit = total.debit.add(nullSafe(line.getDebit()));
                total.credit = total.credit.add(nullSafe(line.getCredit()));
            }
        }

        return totals.values()
                .stream()
                .map(total -> new TrialBalanceLine(
                        total.account.getCode(),
                        total.account.getName(),
                        total.account.getAccountType(),
                        total.debit,
                        total.credit,
                        total.debit.subtract(total.credit)
                ))
                .sorted(Comparator.comparing(TrialBalanceLine::getAccountCode))
                .toList();
    }

    public List<LedgerLine> generalLedger(UUID accountCodeId, LocalDate from, LocalDate to) {
        List<AccountingEntry> entries = loadPostedEntries(from, to);
        List<LedgerLine> ledgerLines = new ArrayList<>();

        for (AccountingEntry entry : entries) {
            List<AccountingEntryLine> lines = lineRepository.findByEntryId(entry.getId());

            for (AccountingEntryLine line : lines) {
                if (line.getAccountCode().getId().equals(accountCodeId)) {
                    ledgerLines.add(new LedgerLine(
                            entry.getEntryNumber(),
                            entry.getEntryDate(),
                            entry.getDescription(),
                            nullSafe(line.getDebit()),
                            nullSafe(line.getCredit())
                    ));
                }
            }
        }

        return ledgerLines.stream()
                .sorted(Comparator.comparing(LedgerLine::getEntryDate))
                .toList();
    }

    private List<AccountingEntry> loadPostedEntries(LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return entryRepository.findByEntryDateBetweenAndStatus(from, to, "POSTED");
        }

        return entryRepository.findByStatus("POSTED");
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static class Totals {
        private final AccountCode account;
        private BigDecimal debit = BigDecimal.ZERO;
        private BigDecimal credit = BigDecimal.ZERO;

        private Totals(AccountCode account) {
            this.account = account;
        }
    }
}