// src/main/java/com/institution/finance/service/PayrollAccountingService.java
package cm.ndicsonlabs.bossapp.ui.payroll;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.payroll.PayrollPeriod;
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
public class PayrollAccountingService {

    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final CurrentUserService currentUserService;

    public PayrollAccountingService(
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
    public void postPayrollAccrual(PayrollPeriod payrollPeriod) {
        if (entryRepository.existsBySourceTypeAndSourceId("PAYROLL_ACCRUAL", payrollPeriod.getId())) {
            return;
        }

        BigDecimal gross = nullSafe(payrollPeriod.getTotalGross());
        BigDecimal deductions = nullSafe(payrollPeriod.getTotalDeductions());
        BigDecimal net = nullSafe(payrollPeriod.getTotalNet());

        if (gross.signum() == 0) {
            return;
        }

        AccountingEntry entry = createEntry(
                payrollPeriod.getEndDate(),
                "Payroll accrual for " + payrollPeriod,
                "PAYROLL_ACCRUAL",
                payrollPeriod.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(line(
                entry,
                getAccount("PAYROLL_EXPENSE"),
                gross,
                BigDecimal.ZERO,
                "Salary expense"
        ));

        if (deductions.signum() > 0) {
            lines.add(line(
                    entry,
                    getAccount("PAYROLL_LIABILITY"),
                    BigDecimal.ZERO,
                    deductions,
                    "Payroll deduction liabilities"
            ));
        }

        if (net.signum() > 0) {
            lines.add(line(
                    entry,
                    getAccount("PAYROLL_PAYABLE"),
                    BigDecimal.ZERO,
                    net,
                    "Net salaries payable"
            ));
        }

        saveEntry(entry, lines);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void postPayrollPayment(PayrollPeriod payrollPeriod, BigDecimal amount) {
        if (entryRepository.existsBySourceTypeAndSourceId("PAYROLL_PAYMENT", payrollPeriod.getId())) {
            return;
        }

        BigDecimal paymentAmount = nullSafe(amount);

        if (paymentAmount.signum() == 0) {
            return;
        }

        AccountingEntry entry = createEntry(
                LocalDate.now(),
                "Payroll payment for " + payrollPeriod,
                "PAYROLL_PAYMENT",
                payrollPeriod.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(line(
                entry,
                getAccount("PAYROLL_PAYABLE"),
                paymentAmount,
                BigDecimal.ZERO,
                "Settlement of salaries payable"
        ));

        lines.add(line(
                entry,
                getAccount("PAYROLL_CASH"),
                BigDecimal.ZERO,
                paymentAmount,
                "Payroll cash payment"
        ));

        saveEntry(entry, lines);
    }

    private AccountingEntry createEntry(LocalDate entryDate, String description, String sourceType, UUID sourceId) {
        AccountingPeriod accountingPeriod = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(entryDate, entryDate)
                .orElseThrow(() -> new IllegalStateException("No accounting period found for date: " + entryDate));

        if (!"OPEN".equals(accountingPeriod.getStatus())) {
            throw new IllegalStateException("Accounting period is not open for date: " + entryDate);
        }

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
            String description
    ) {
        AccountingEntryLine line = new AccountingEntryLine();
        line.setEntry(entry);
        line.setAccountCode(account);
        line.setDebit(debit);
        line.setCredit(credit);
        line.setDescription(description);

        return line;
    }

    private void saveEntry(AccountingEntry entry, List<AccountingEntryLine> lines) {
        BigDecimal totalDebit = lines.stream()
                .map(line -> nullSafe(line.getDebit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = lines.stream()
                .map(line -> nullSafe(line.getCredit()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("Payroll accounting entry is not balanced.");
        }

        entryRepository.save(entry);
        lineRepository.saveAll(lines);
    }

    private AccountCode getAccount(String mappingType) {
        AccountMapping mapping = accountMappingRepository.findByMappingType(mappingType)
                .orElseThrow(() -> new IllegalStateException("Account mapping not found: " + mappingType));

        return mapping.getAccountCode();
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}