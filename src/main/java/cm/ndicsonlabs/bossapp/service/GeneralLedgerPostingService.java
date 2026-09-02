// src/main/java/com/institution/finance/service/GeneralLedgerPostingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.*;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GeneralLedgerPostingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final CurrencyConversionService currencyConversionService;
    private final PeriodValidationService periodValidationService;
    private final CurrentUserService currentUserService;

    @Value("${app.finance.base-currency:USD}")
    private String baseCurrency;

    public GeneralLedgerPostingService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            CurrencyConversionService currencyConversionService,
            PeriodValidationService periodValidationService,
            CurrentUserService currentUserService
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.currencyConversionService = currencyConversionService;
        this.periodValidationService = periodValidationService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AccountingEntry post(PostingRequest request) {
        AccountingPeriod period = periodValidationService.getOpenPeriodForPosting(request.date());

        if (entryRepository.existsBySourceTypeAndSourceId(request.sourceType(), request.sourceId())) {
            throw new IllegalStateException(
                    "Accounting entry already exists for source: " + request.sourceType() +
                    " / " + request.sourceId()
            );
        }

        AccountingEntry entry = new AccountingEntry();
        entry.setEntryNumber("JE-" + UUID.randomUUID());
        entry.setEntryDate(request.date());
        entry.setAccountingPeriod(period);
        entry.setDepartment(request.department());
        entry.setDescription(request.description());
        entry.setSourceType(request.sourceType());
        entry.setSourceId(request.sourceId());
        entry.setStatus("POSTED");
        entry.setPostedBy(currentUserService.username());
        entry.setPostedAt(Instant.now());
        entry.setTransactionCurrency(request.currency());
        entry.setBaseCurrency(baseCurrency);
        entry.setExchangeRate(currencyConversionService.convert(
                BigDecimal.ONE,
                request.currency(),
                baseCurrency,
                request.date()
        ));

        List<AccountingEntryLine> lines = new ArrayList<>();

        for (PostingLine line : request.lines()) {
            lines.add(buildLine(entry, request, line));
        }

        validateBalanced(lines);

        entryRepository.save(entry);
        lineRepository.saveAll(lines);

        return entry;
    }

    private AccountingEntryLine buildLine(
            AccountingEntry entry,
            PostingRequest request,
            PostingLine line
    ) {
        String lineCurrency = line.currency() != null ? line.currency() : request.currency();

        BigDecimal debitBase = currencyConversionService.convert(
                line.debit(),
                lineCurrency,
                baseCurrency,
                request.date()
        );

        BigDecimal creditBase = currencyConversionService.convert(
                line.credit(),
                lineCurrency,
                baseCurrency,
                request.date()
        );

        AccountingEntryLine entryLine = new AccountingEntryLine();
        entryLine.setEntry(entry);
        entryLine.setAccountCode(line.accountCode());
        entryLine.setDepartment(line.department() != null ? line.department() : request.department());
        entryLine.setDescription(line.description());
        entryLine.setDebit(debitBase);
        entryLine.setCredit(creditBase);
        entryLine.setCurrency(lineCurrency);
        entryLine.setExchangeRate(currencyConversionService.convert(
                BigDecimal.ONE,
                lineCurrency,
                baseCurrency,
                request.date()
        ));
        entryLine.setDebitCurrency(line.debit());
        entryLine.setCreditCurrency(line.credit());
        entryLine.setTaxCode(line.taxCode());
        entryLine.setTaxBasis(line.taxBasis());
        entryLine.setTaxAmount(line.taxAmount());

        return entryLine;
    }

    private void validateBalanced(List<AccountingEntryLine> lines) {
        BigDecimal totalDebit = lines.stream()
                .map(line -> line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .map(line -> line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException(
                    "Accounting entry is not balanced. Debits: " + totalDebit + ", Credits: " + totalCredit
            );
        }
    }

    public record PostingRequest(
            LocalDate date,
            String sourceType,
            UUID sourceId,
            Department department,
            String currency,
            String description,
            List<PostingLine> lines
    ) {
    }

    public record PostingLine(
            AccountCode accountCode,
            Department department,
            String currency,
            BigDecimal debit,
            BigDecimal credit,
            String description,
            TaxCode taxCode,
            BigDecimal taxBasis,
            BigDecimal taxAmount
    ) {
    }
}