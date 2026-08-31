// src/main/java/com/institution/finance/service/TreasuryService.java
package cm.ndicsonlabs.bossapp.service.treasury;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import cm.ndicsonlabs.bossapp.dto.CashPositionLine;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryTransactionRepository;
import cm.ndicsonlabs.bossapp.service.AuditService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TreasuryService {

    private final TreasuryAccountRepository accountRepository;
    private final TreasuryTransactionRepository transactionRepository;
    private final DepartmentRepository departmentRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public TreasuryService(
            TreasuryAccountRepository accountRepository,
            TreasuryTransactionRepository transactionRepository,
            DepartmentRepository departmentRepository,
            PaymentRepository paymentRepository,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.departmentRepository = departmentRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public TreasuryAccount createAccount(
            String code,
            String name,
            String accountType,
            String currency,
            UUID departmentId,
            String bankName,
            String accountNumber,
            String iban,
            String swift,
            BigDecimal openingBalance,
            boolean allowNegative
    ) {
        requireTreasuryPrivilege();

        Department department = null;

        if (departmentId != null) {
            department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found"));
        }

        TreasuryAccount account = new TreasuryAccount();
        account.setCode(code);
        account.setName(name);
        account.setAccountType(accountType);
        account.setCurrency(currency);
        account.setDepartment(department);
        account.setBankName(bankName);
        account.setAccountNumber(accountNumber);
        account.setIban(iban);
        account.setSwift(swift);
        account.setOpeningBalance(openingBalance != null ? openingBalance : BigDecimal.ZERO);
        account.setCurrentBalance(account.getOpeningBalance());
        account.setAllowNegative(allowNegative);
        account.setActive(true);

        accountRepository.save(account);

        auditService.log(
                "TREASURY_ACCOUNT",
                account.getId(),
                "CREATE_TREASURY_ACCOUNT",
                null,
                account.getCode(),
                "Treasury account created"
        );

        return account;
    }

    @Transactional
    public TreasuryTransaction postManualTransaction(
            UUID accountId,
            String direction,
            BigDecimal amount,
            LocalDate transactionDate,
            String reference,
            String description
    ) {
        requireTreasuryPrivilege();

        return postTransaction(
                accountId,
                null,
                direction,
                amount,
                transactionDate,
                reference,
                description,
                "MANUAL",
                null
        );
    }

    @Transactional
    public TreasuryTransaction postPaymentToAccount(UUID paymentId, UUID accountId) {
        requireTreasuryPrivilege();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (transactionRepository.existsBySourceTypeAndSourceId("PAYMENT", paymentId)) {
            throw new IllegalStateException("This payment has already been posted to a treasury account.");
        }

        return postTransaction(
                accountId,
                payment,
                payment.getDirection(),
                payment.getAmount(),
                payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now(),
                payment.getPaymentNumber(),
                "Payment posting",
                "PAYMENT",
                paymentId
        );
    }

    public List<TreasuryTransaction> cashbook(UUID accountId, LocalDate from, LocalDate to) {
        if (from != null && to != null) {
            return transactionRepository.findByTreasuryAccountIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                    accountId,
                    from,
                    to
            );
        }

        return transactionRepository.findByTreasuryAccountIdOrderByTransactionDateDesc(accountId);
    }

    public List<CashPositionLine> cashPositionByDepartment() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, BigDecimal> balances = new LinkedHashMap<>();

        for (TreasuryAccount account : accountRepository.findByActiveTrueOrderByCode()) {
            String departmentName = account.getDepartment() != null
                    ? account.getDepartment().getName()
                    : "Institution-Level";

            counts.put(departmentName, counts.getOrDefault(departmentName, 0) + 1);

            balances.put(
                    departmentName,
                    balances.getOrDefault(departmentName, BigDecimal.ZERO)
                            .add(account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO)
            );
        }

        List<CashPositionLine> lines = new ArrayList<>();

        for (String departmentName : counts.keySet()) {
            lines.add(new CashPositionLine(
                    departmentName,
                    counts.get(departmentName),
                    balances.getOrDefault(departmentName, BigDecimal.ZERO)
            ));
        }

        return lines.stream()
                .sorted(Comparator.comparing(CashPositionLine::getDepartmentName))
                .toList();
    }

    private TreasuryTransaction postTransaction(
            UUID accountId,
            Payment payment,
            String direction,
            BigDecimal amount,
            LocalDate transactionDate,
            String reference,
            String description,
            String sourceType,
            UUID sourceId
    ) {
        TreasuryAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Treasury account not found"));

        if (!account.isActive()) {
            throw new IllegalStateException("Treasury account is inactive.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Treasury transaction amount must be greater than zero.");
        }

        if (!"IN".equals(direction) && !"OUT".equals(direction)) {
            throw new IllegalArgumentException("Treasury transaction direction must be IN or OUT.");
        }

        TreasuryTransaction transaction = new TreasuryTransaction();
        transaction.setTransactionNumber("TRN-" + UUID.randomUUID());
        transaction.setTreasuryAccount(account);
        transaction.setPayment(payment);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setCurrency(account.getCurrency());
        transaction.setTransactionDate(transactionDate != null ? transactionDate : LocalDate.now());
        transaction.setValueDate(transaction.getTransactionDate());
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setStatus("UNCLEARED");
        transaction.setSourceType(sourceType);
        transaction.setSourceId(sourceId);
        transaction.setCreatedBy(currentUserService.username());

        updateAccountBalance(account, direction, amount);

        transactionRepository.save(transaction);

        auditService.log(
                "TREASURY_TRANSACTION",
                transaction.getId(),
                "POST_TREASURY_TRANSACTION",
                null,
                transaction.getTransactionNumber(),
                description
        );

        return transaction;
    }

    private void updateAccountBalance(TreasuryAccount account, String direction, BigDecimal amount) {
        BigDecimal current = account.getCurrentBalance() != null ? account.getCurrentBalance() : BigDecimal.ZERO;

        BigDecimal newBalance = "IN".equals(direction)
                ? current.add(amount)
                : current.subtract(amount);

        if (!account.isAllowNegative() && newBalance.signum() < 0) {
            throw new IllegalStateException(
                    "Treasury account balance cannot become negative. Current balance: " + current
            );
        }

        account.setCurrentBalance(newBalance);
        accountRepository.save(account);
    }

    private void requireTreasuryPrivilege() {
        if (!currentUserService.hasPrivilege("TREASURY_MANAGE")) {
            throw new AccessDeniedException("Current user does not have treasury management privilege.");
        }
    }
}