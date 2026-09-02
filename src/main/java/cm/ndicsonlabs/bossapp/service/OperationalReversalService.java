// src/main/java/com/institution/finance/service/OperationalReversalService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.domain.OperationalReversal;
import cm.ndicsonlabs.bossapp.repository.OperationalReversalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OperationalReversalService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final OperationalReversalRepository reversalRepository;
    private final PeriodValidationService periodValidationService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public OperationalReversalService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            OperationalReversalRepository reversalRepository,
            PeriodValidationService periodValidationService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.reversalRepository = reversalRepository;
        this.periodValidationService = periodValidationService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public AccountingEntry reverseEntry(UUID entryId, String reason, boolean automatic) {
        AccountingEntry original = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting entry not found"));

        if (original.getReversedByEntryId() != null) {
            throw new IllegalStateException("Accounting entry is already reversed.");
        }

        if (!"POSTED".equals(original.getStatus())) {
            throw new IllegalStateException("Only posted entries can be reversed.");
        }

        AccountingPeriod period = periodValidationService.getOpenPeriodForPosting(LocalDate.now());

        AccountingEntry reversal = new AccountingEntry();
        reversal.setEntryNumber("JE-REV-" + UUID.randomUUID());
        reversal.setEntryDate(LocalDate.now());
        reversal.setAccountingPeriod(period);
        reversal.setDepartment(original.getDepartment());
        reversal.setDescription("Reversal of " + original.getEntryNumber());
        reversal.setSourceType("ENTRY_REVERSAL");
        reversal.setSourceId(original.getSourceId());
        reversal.setOriginalEntryId(original.getId());
        reversal.setReversalReason(reason);
        reversal.setAutoReversed(automatic);
        reversal.setStatus("POSTED");
        reversal.setPostedBy(currentUserService.username());
        reversal.setPostedAt(Instant.now());
        reversal.setTransactionCurrency(original.getTransactionCurrency());
        reversal.setBaseCurrency(original.getBaseCurrency());
        reversal.setExchangeRate(original.getExchangeRate());

        List<AccountingEntryLine> originalLines = lineRepository.findByEntryId(original.getId());
        List<AccountingEntryLine> reversalLines = new ArrayList<>();

        for (AccountingEntryLine originalLine : originalLines) {
            AccountingEntryLine line = new AccountingEntryLine();
            line.setEntry(reversal);
            line.setAccountCode(originalLine.getAccountCode());
            line.setDepartment(originalLine.getDepartment());
            line.setDescription("Reversal: " + originalLine.getDescription());
            line.setDebit(originalLine.getCredit());
            line.setCredit(originalLine.getDebit());
            line.setCurrency(originalLine.getCurrency());
            line.setExchangeRate(originalLine.getExchangeRate());
            line.setDebitCurrency(originalLine.getCreditCurrency());
            line.setCreditCurrency(originalLine.getDebitCurrency());
            line.setTaxCode(originalLine.getTaxCode());
            line.setTaxBasis(originalLine.getTaxBasis());
            line.setTaxAmount(originalLine.getTaxAmount());

            reversalLines.add(line);
        }

        entryRepository.save(reversal);
        lineRepository.saveAll(reversalLines);

        original.setReversedByEntryId(reversal.getId());
        original.setStatus("REVERSED");
        original.setReversalReason(reason);
        original.setAutoReversed(automatic);

        entryRepository.save(original);

        auditService.log(
                "ACCOUNTING_ENTRY",
                original.getId(),
                "REVERSE_ENTRY",
                null,
                reversal.getEntryNumber(),
                reason
        );

        return reversal;
    }

    @Transactional
    public int detectAndReverseDuplicatePostings(boolean automaticExecutionEnabled) {
        List<Object[]> duplicates = entryRepository.findDuplicatePostedSources();

        int reversedCount = 0;

        for (Object[] row : duplicates) {
            String sourceType = (String) row[0];
            UUID sourceId = (UUID) row[1];

            List<AccountingEntry> entries = entryRepository
                    .findBySourceTypeAndSourceIdAndStatusOrderByCreatedAtAsc(sourceType, sourceId, "POSTED");

            if (entries.size() <= 1) {
                continue;
            }

            for (int i = 1; i < entries.size(); i++) {
                AccountingEntry duplicate = entries.get(i);

                OperationalReversal reversal = new OperationalReversal();
                reversal.setSourceType(sourceType);
                reversal.setSourceId(sourceId);
                reversal.setAccountingEntry(duplicate);
                reversal.setReason("Duplicate accounting entry detected.");
                reversal.setStatus("DETECTED");
                reversal.setAutomatic(automaticExecutionEnabled);

                if (automaticExecutionEnabled) {
                    AccountingEntry reversalEntry = reverseEntry(
                            duplicate.getId(),
                            "Automatic reversal of duplicate posting",
                            true
                    );

                    reversal.setReversalEntry(reversalEntry);
                    reversal.setStatus("REVERSED");
                    reversal.setReversedAt(Instant.now());
                    reversal.setReversedBy(currentUserService.username());

                    reversedCount++;
                }

                reversalRepository.save(reversal);
            }
        }

        return reversedCount;
    }
}