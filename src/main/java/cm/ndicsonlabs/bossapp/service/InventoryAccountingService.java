// src/main/java/com/institution/finance/service/InventoryAccountingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.InventoryTransaction;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
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
public class InventoryAccountingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final CurrentUserService currentUserService;

    public InventoryAccountingService(
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
    public void postReceipt(InventoryTransaction transaction) {
        postMovement(
                transaction,
                "Inventory receipt: " + transaction.getTransactionNumber(),
                "INVENTORY_ASSET",
                "INVENTORY_RECEIPT_CLEARING"
        );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postIssue(InventoryTransaction transaction, boolean writeOff) {
        String debitMapping = writeOff ? "INVENTORY_WRITE_OFF" : "COST_OF_GOODS_CONSUMED";

        postMovement(
                transaction,
                "Inventory issue: " + transaction.getTransactionNumber(),
                debitMapping,
                "INVENTORY_ASSET"
        );
    }

    private void postMovement(
            InventoryTransaction transaction,
            String description,
            String debitMappingType,
            String creditMappingType
    ) {
        BigDecimal amount = transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO;

        if (amount.signum() == 0) {
            return;
        }

        if (entryRepository.existsBySourceTypeAndSourceId("INVENTORY_TRANSACTION", transaction.getId())) {
            return;
        }

        AccountingPeriod period = getOpenPeriod(transaction.getTransactionDate());

        AccountingEntry entry = new AccountingEntry();
        entry.setEntryNumber("JE-" + UUID.randomUUID());
        entry.setEntryDate(transaction.getTransactionDate());
        entry.setAccountingPeriod(period);
        entry.setDepartment(transaction.getLocation().getDepartment());
        entry.setDescription(description);
        entry.setSourceType("INVENTORY_TRANSACTION");
        entry.setSourceId(transaction.getId());
        entry.setStatus("POSTED");
        entry.setPostedBy(currentUserService.username());
        entry.setPostedAt(Instant.now());

        AccountCode debitAccount = getAccount(debitMappingType);
        AccountCode creditAccount = getAccount(creditMappingType);

        List<AccountingEntryLine> lines = new ArrayList<>();

        AccountingEntryLine debitLine = new AccountingEntryLine();
        debitLine.setEntry(entry);
        debitLine.setAccountCode(debitAccount);
        debitLine.setDebit(amount);
        debitLine.setCredit(BigDecimal.ZERO);
        debitLine.setDepartment(transaction.getLocation().getDepartment());
        debitLine.setDescription(description);
        lines.add(debitLine);

        AccountingEntryLine creditLine = new AccountingEntryLine();
        creditLine.setEntry(entry);
        creditLine.setAccountCode(creditAccount);
        creditLine.setDebit(BigDecimal.ZERO);
        creditLine.setCredit(amount);
        creditLine.setDepartment(transaction.getLocation().getDepartment());
        creditLine.setDescription(description);
        lines.add(creditLine);

        entryRepository.save(entry);
        lineRepository.saveAll(lines);
    }

    private AccountCode getAccount(String mappingType) {
        AccountMapping mapping = accountMappingRepository.findByMappingType(mappingType)
                .orElseThrow(() -> new IllegalStateException("Account mapping not found: " + mappingType));

        return mapping.getAccountCode();
    }

    private AccountingPeriod getOpenPeriod(LocalDate date) {
        AccountingPeriod period = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .orElseThrow(() -> new IllegalStateException("No accounting period found for date: " + date));

        if (!"OPEN".equals(period.getStatus())) {
            throw new IllegalStateException("Accounting period is not open for date: " + date);
        }

        return period;
    }
}