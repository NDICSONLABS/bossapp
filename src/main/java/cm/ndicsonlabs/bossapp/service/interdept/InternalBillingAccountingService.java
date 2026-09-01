// src/main/java/com/institution/finance/service/InternalBillingAccountingService.java
package cm.ndicsonlabs.bossapp.service.interdept;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRun;
import cm.ndicsonlabs.bossapp.domain.interdept.CostAllocationRunLine;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalSettlement;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InternalBillingAccountingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final CurrentUserService currentUserService;

    public InternalBillingAccountingService(
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            AccountingPeriodRepository periodRepository,
            AccountMappingRepository accountMappingRepository,
            CurrentUserService currentUserService
    ) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.periodRepository = periodRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postInternalInvoice(InternalInvoice invoice) {
        if (entryRepository.existsBySourceTypeAndSourceId("INTERNAL_INVOICE", invoice.getId())) {
            return;
        }

        AccountingEntry entry = createEntry(
                invoice.getTransactionDate(),
                "Internal invoice " + invoice.getInvoiceNumber(),
                "INTERNAL_INVOICE",
                invoice.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(line(
                entry,
                getAccount("INTERDEPT_RECEIVABLE"),
                invoice.getAmount(),
                BigDecimal.ZERO,
                invoice.getProviderDepartment(),
                "Internal receivable"
        ));

        lines.add(line(
                entry,
                getAccount("INTERNAL_REVENUE"),
                BigDecimal.ZERO,
                invoice.getAmount(),
                invoice.getProviderDepartment(),
                "Internal service revenue"
        ));

        lines.add(line(
                entry,
                getAccount("INTERNAL_EXPENSE"),
                invoice.getAmount(),
                BigDecimal.ZERO,
                invoice.getReceiverDepartment(),
                "Internal service expense"
        ));

        lines.add(line(
                entry,
                getAccount("INTERDEPT_PAYABLE"),
                BigDecimal.ZERO,
                invoice.getAmount(),
                invoice.getReceiverDepartment(),
                "Internal payable"
        ));

        saveEntry(entry, lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postInternalSettlement(InternalSettlement settlement) {
        if (entryRepository.existsBySourceTypeAndSourceId("INTERNAL_SETTLEMENT", settlement.getId())) {
            return;
        }

        AccountingEntry entry = createEntry(
                settlement.getSettlementDate(),
                "Internal settlement " + settlement.getReference(),
                "INTERNAL_SETTLEMENT",
                settlement.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(line(
                entry,
                getAccount("INTERDEPT_PAYABLE"),
                settlement.getAmount(),
                BigDecimal.ZERO,
                settlement.getReceiverDepartment(),
                "Clear internal payable"
        ));

        lines.add(line(
                entry,
                getAccount("INTERDEPT_RECEIVABLE"),
                BigDecimal.ZERO,
                settlement.getAmount(),
                settlement.getProviderDepartment(),
                "Clear internal receivable"
        ));

        saveEntry(entry, lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postCostAllocation(
            CostAllocationRun run,
            List<CostAllocationRunLine> lines,
            Department sourceDepartment,
            LocalDate allocationDate
    ) {
        if (entryRepository.existsBySourceTypeAndSourceId("COST_ALLOCATION", run.getId())) {
            return;
        }

        AccountingEntry entry = createEntry(
                allocationDate,
                "Cost allocation run for " + run.getRule().getName(),
                "COST_ALLOCATION",
                run.getId()
        );

        List<AccountingEntryLine> entryLines = new ArrayList<>();

        for (CostAllocationRunLine line : lines) {
            entryLines.add(line(
                    entry,
                    getAccount("INTERNAL_EXPENSE"),
                    line.getAmount(),
                    BigDecimal.ZERO,
                    line.getReceiverDepartment(),
                    "Allocated cost to " + line.getReceiverDepartment().getName()
            ));
        }

        entryLines.add(line(
                entry,
                getAccount("INTERNAL_EXPENSE"),
                BigDecimal.ZERO,
                run.getTotalAmount(),
                sourceDepartment,
                "Cost reallocated from source department"
        ));

        saveEntry(entry, entryLines);
    }

    private AccountingEntry createEntry(LocalDate entryDate, String description, String sourceType, UUID sourceId) {
        AccountingPeriod accountingPeriod = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(entryDate, entryDate)
                .orElseThrow(() -> new IllegalStateException("No open accounting period for date: " + entryDate));

        AccountingEntry entry = new AccountingEntry();
        entry.setEntryNumber("JE-" + UUID.randomUUID());
        entry.setEntryDate(entryDate);
        entry.setAccountingPeriod(accountingPeriod);
        entry.setDescription(description);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setStatus("POSTED");
        entry.setPostedBy(currentUserService.username());
        entry.setPostedAt(Instant.now());

        return entry;
    }

    private AccountingEntryLine line(
            AccountingEntry entry,
            AccountCode account,
            BigDecimal debit,
            BigDecimal credit,
            Department department,
            String description
    ) {
        AccountingEntryLine line = new AccountingEntryLine();
        line.setEntry(entry);
        line.setAccountCode(account);
        line.setDebit(debit);
        line.setCredit(credit);
        line.setDepartment(department);
        line.setDescription(description);

        return line;
    }

    private void saveEntry(AccountingEntry entry, List<AccountingEntryLine> lines) {
        BigDecimal totalDebit = lines.stream()
                .map(line -> line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .map(line -> line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("Internal accounting entry is not balanced.");
        }

        entryRepository.save(entry);
        lineRepository.saveAll(lines);
    }

    private AccountCode getAccount(String mappingType) {
        return accountMappingRepository.findByMappingType(mappingType)
                .orElseThrow(() -> new IllegalStateException("Account mapping not found: " + mappingType))
                .getAccountCode();
    }
}