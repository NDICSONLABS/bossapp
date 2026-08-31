// src/main/java/com/institution/finance/service/BankReconciliationService.java
package cm.ndicsonlabs.bossapp.service.treasury;

import cm.ndicsonlabs.bossapp.domain.treasury.BankReconciliation;
import cm.ndicsonlabs.bossapp.domain.treasury.BankStatement;
import cm.ndicsonlabs.bossapp.domain.treasury.BankStatementLine;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryAccount;
import cm.ndicsonlabs.bossapp.domain.treasury.TreasuryTransaction;
import cm.ndicsonlabs.bossapp.repository.treasury.BankReconciliationRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementLineRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.BankStatementRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryAccountRepository;
import cm.ndicsonlabs.bossapp.repository.treasury.TreasuryTransactionRepository;
import cm.ndicsonlabs.bossapp.service.AuditService;
import cm.ndicsonlabs.bossapp.service.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class BankReconciliationService {

    private final TreasuryAccountRepository accountRepository;
    private final TreasuryTransactionRepository transactionRepository;
    private final BankStatementRepository statementRepository;
    private final BankStatementLineRepository statementLineRepository;
    private final BankReconciliationRepository reconciliationRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public BankReconciliationService(
            TreasuryAccountRepository accountRepository,
            TreasuryTransactionRepository transactionRepository,
            BankStatementRepository statementRepository,
            BankStatementLineRepository statementLineRepository,
            BankReconciliationRepository reconciliationRepository,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.statementRepository = statementRepository;
        this.statementLineRepository = statementLineRepository;
        this.reconciliationRepository = reconciliationRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public BankStatement createStatement(
            UUID accountId,
            String statementNumber,
            LocalDate statementDate,
            BigDecimal openingBalance,
            BigDecimal closingBalance
    ) {
        requireTreasuryPrivilege();

        TreasuryAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Treasury account not found"));

        BankStatement statement = new BankStatement();
        statement.setTreasuryAccount(account);
        statement.setStatementNumber(statementNumber);
        statement.setStatementDate(statementDate);
        statement.setOpeningBalance(openingBalance != null ? openingBalance : BigDecimal.ZERO);
        statement.setClosingBalance(closingBalance != null ? closingBalance : BigDecimal.ZERO);
        statement.setCurrency(account.getCurrency());
        statement.setStatus("UPLOADED");
        statement.setUploadedBy(currentUserService.username());

        statementRepository.save(statement);

        auditService.log(
                "BANK_STATEMENT",
                statement.getId(),
                "CREATE_BANK_STATEMENT",
                null,
                statement.getStatementNumber(),
                "Bank statement created"
        );

        return statement;
    }

    @Transactional
    public BankStatementLine addStatementLine(
            UUID statementId,
            Integer lineNumber,
            LocalDate transactionDate,
            BigDecimal amount,
            String direction,
            String reference,
            String description
    ) {
        requireTreasuryPrivilege();

        BankStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Bank statement not found"));

        if ("RECONCILED".equals(statement.getStatus())) {
            throw new IllegalStateException("Cannot add lines to a reconciled bank statement.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Statement line amount must be greater than zero.");
        }

        if (!"IN".equals(direction) && !"OUT".equals(direction)) {
            throw new IllegalArgumentException("Statement line direction must be IN or OUT.");
        }

        BankStatementLine line = new BankStatementLine();
        line.setBankStatement(statement);
        line.setLineNumber(lineNumber);
        line.setTransactionDate(transactionDate);
        line.setAmount(amount);
        line.setDirection(direction);
        line.setReference(reference);
        line.setDescription(description);
        line.setStatus("UNMATCHED");

        statementLineRepository.save(line);

        auditService.log(
                "BANK_STATEMENT_LINE",
                line.getId(),
                "ADD_STATEMENT_LINE",
                null,
                statement.getStatementNumber(),
                "Bank statement line added"
        );

        return line;
    }

    @Transactional
    public BankReconciliation prepareReconciliation(UUID statementId) {
        requireBankReconciliationPrivilege();

        BankStatement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new IllegalArgumentException("Bank statement not found"));

        reconciliationRepository.findFirstByBankStatementIdOrderByCreatedAtDesc(statementId)
                .ifPresent(existing -> {
                    if ("OPEN".equals(existing.getStatus())) {
                        throw new IllegalStateException("An open reconciliation already exists for this statement.");
                    }
                });

        TreasuryAccount account = statement.getTreasuryAccount();

        BigDecimal cashbookBalance = calculateCashbookBalance(account, statement.getStatementDate());
        BigDecimal variance = nullSafe(statement.getClosingBalance()).subtract(cashbookBalance);

        BankReconciliation reconciliation = new BankReconciliation();
        reconciliation.setTreasuryAccount(account);
        reconciliation.setBankStatement(statement);
        reconciliation.setStatementDate(statement.getStatementDate());
        reconciliation.setStatementClosingBalance(nullSafe(statement.getClosingBalance()));
        reconciliation.setCashbookBalance(cashbookBalance);
        reconciliation.setAdjustedBalance(cashbookBalance);
        reconciliation.setVariance(variance);
        reconciliation.setStatus("OPEN");
        reconciliation.setPreparedBy(currentUserService.username());

        reconciliationRepository.save(reconciliation);

        auditService.log(
                "BANK_RECONCILIATION",
                reconciliation.getId(),
                "PREPARE_RECONCILIATION",
                null,
                statement.getStatementNumber(),
                "Bank reconciliation prepared"
        );

        return reconciliation;
    }

    @Transactional
    public void matchLine(UUID statementLineId, UUID treasuryTransactionId) {
        requireBankReconciliationPrivilege();

        BankStatementLine line = statementLineRepository.findById(statementLineId)
                .orElseThrow(() -> new IllegalArgumentException("Bank statement line not found"));

        TreasuryTransaction transaction = transactionRepository.findById(treasuryTransactionId)
                .orElseThrow(() -> new IllegalArgumentException("Treasury transaction not found"));

        if (!"UNMATCHED".equals(line.getStatus())) {
            throw new IllegalStateException("Only unmatched bank statement lines can be matched.");
        }

        if (!"UNCLEARED".equals(transaction.getStatus())) {
            throw new IllegalStateException("Only uncleared treasury transactions can be matched.");
        }

        if (!line.getBankStatement().getTreasuryAccount().getId()
                .equals(transaction.getTreasuryAccount().getId())) {
            throw new IllegalStateException("Statement line and treasury transaction belong to different accounts.");
        }

        if (nullSafe(line.getAmount()).compareTo(nullSafe(transaction.getAmount())) != 0) {
            throw new IllegalStateException("Statement line amount does not match treasury transaction amount.");
        }

        if (!line.getDirection().equals(transaction.getDirection())) {
            throw new IllegalStateException("Statement line direction does not match treasury transaction direction.");
        }

        line.setMatchedTreasuryTransaction(transaction);
        line.setStatus("MATCHED");
        statementLineRepository.save(line);

        transaction.setStatus("CLEARED");
        transactionRepository.save(transaction);

        auditService.log(
                "BANK_STATEMENT_LINE",
                line.getId(),
                "MATCH_STATEMENT_LINE",
                null,
                transaction.getTransactionNumber(),
                "Bank statement line matched"
        );
    }

    @Transactional
    public void ignoreLine(UUID statementLineId, String reason) {
        requireBankReconciliationPrivilege();

        BankStatementLine line = statementLineRepository.findById(statementLineId)
                .orElseThrow(() -> new IllegalArgumentException("Bank statement line not found"));

        if (!"UNMATCHED".equals(line.getStatus())) {
            throw new IllegalStateException("Only unmatched bank statement lines can be ignored.");
        }

        line.setStatus("IGNORED");
        line.setIgnoreReason(reason);
        statementLineRepository.save(line);

        auditService.log(
                "BANK_STATEMENT_LINE",
                line.getId(),
                "IGNORE_STATEMENT_LINE",
                null,
                line.getBankStatement().getStatementNumber(),
                reason
        );
    }

    @Transactional
    public BankReconciliation completeReconciliation(UUID reconciliationId) {
        requireBankReconciliationPrivilege();

        BankReconciliation reconciliation = reconciliationRepository.findById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Bank reconciliation not found"));

        if (!"OPEN".equals(reconciliation.getStatus())) {
            throw new IllegalStateException("Only open reconciliations can be completed.");
        }

        long unmatched = statementLineRepository.countByBankStatementIdAndStatus(
                reconciliation.getBankStatement().getId(),
                "UNMATCHED"
        );

        if (unmatched > 0) {
            throw new IllegalStateException("Cannot complete reconciliation while unmatched statement lines remain.");
        }

        reconciliation.setStatus("COMPLETED");
        reconciliation.setAdjustedBalance(reconciliation.getStatementClosingBalance());

        reconciliationRepository.save(reconciliation);

        BankStatement statement = reconciliation.getBankStatement();
        statement.setStatus("RECONCILED");
        statementRepository.save(statement);

        auditService.log(
                "BANK_RECONCILIATION",
                reconciliation.getId(),
                "COMPLETE_RECONCILIATION",
                null,
                reconciliation.getBankStatement().getStatementNumber(),
                "Bank reconciliation completed"
        );

        return reconciliation;
    }

    @Transactional
    public BankReconciliation approveReconciliation(UUID reconciliationId) {
        requireBankReconciliationPrivilege();

        BankReconciliation reconciliation = reconciliationRepository.findById(reconciliationId)
                .orElseThrow(() -> new IllegalArgumentException("Bank reconciliation not found"));

        if (!"COMPLETED".equals(reconciliation.getStatus())) {
            throw new IllegalStateException("Only completed reconciliations can be approved.");
        }

        reconciliation.setStatus("APPROVED");
        reconciliation.setApprovedBy(currentUserService.username());
        reconciliation.setApprovedAt(Instant.now());

        reconciliationRepository.save(reconciliation);

        auditService.log(
                "BANK_RECONCILIATION",
                reconciliation.getId(),
                "APPROVE_RECONCILIATION",
                null,
                reconciliation.getBankStatement().getStatementNumber(),
                "Bank reconciliation approved"
        );

        return reconciliation;
    }

    public List<BankStatementLine> unmatchedLines(UUID statementId) {
        return statementLineRepository.findByBankStatementIdAndStatusOrderByLineNumberAsc(
                statementId,
                "UNMATCHED"
        );
    }

    public List<TreasuryTransaction> unmatchedTransactions(UUID accountId, LocalDate statementDate) {
        return transactionRepository.findByTreasuryAccountIdAndStatusAndTransactionDateLessThanEqualOrderByTransactionDateAsc(
                accountId,
                "UNCLEARED",
                statementDate
        );
    }

    private BigDecimal calculateCashbookBalance(TreasuryAccount account, LocalDate asOf) {
        BigDecimal balance = nullSafe(account.getOpeningBalance());

        List<TreasuryTransaction> transactions = transactionRepository
                .findByTreasuryAccountIdAndTransactionDateLessThanEqual(account.getId(), asOf);

        for (TreasuryTransaction transaction : transactions) {
            if ("IN".equals(transaction.getDirection())) {
                balance = balance.add(nullSafe(transaction.getAmount()));
            } else if ("OUT".equals(transaction.getDirection())) {
                balance = balance.subtract(nullSafe(transaction.getAmount()));
            }
        }

        return balance;
    }

    private void requireTreasuryPrivilege() {
        if (!currentUserService.hasPrivilege("TREASURY_MANAGE")) {
            throw new AccessDeniedException("Current user does not have treasury management privilege.");
        }
    }

    private void requireBankReconciliationPrivilege() {
        if (!currentUserService.hasPrivilege("BANK_RECONCILE")) {
            throw new AccessDeniedException("Current user does not have bank reconciliation privilege.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}