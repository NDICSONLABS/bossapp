// src/main/java/com/institution/finance/service/CashierService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.CashierSession;
import cm.ndicsonlabs.bossapp.domain.CashierTransaction;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.repository.CashierSessionRepository;
import cm.ndicsonlabs.bossapp.repository.CashierTransactionRepository;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CashierService {

    private final CashierSessionRepository cashierSessionRepository;
    private final CashierTransactionRepository cashierTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final DepartmentRepository departmentRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public CashierService(
            CashierSessionRepository cashierSessionRepository,
            CashierTransactionRepository cashierTransactionRepository,
            PaymentRepository paymentRepository,
            DepartmentRepository departmentRepository,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.cashierSessionRepository = cashierSessionRepository;
        this.cashierTransactionRepository = cashierTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.departmentRepository = departmentRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public CashierSession openSession(UUID departmentId, LocalDate sessionDate, BigDecimal openingBalance) {
        requireCashierPrivilege();

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        if (cashierSessionRepository.existsByDepartmentAndSessionDate(department, sessionDate)) {
            throw new IllegalStateException("A cashier session already exists for this department and date.");
        }

        CashierSession session = new CashierSession();
        session.setDepartment(department);
        session.setSessionDate(sessionDate);
        session.setCashierUsername(currentUserService.username());
        session.setOpeningBalance(openingBalance != null ? openingBalance : BigDecimal.ZERO);
        session.setStatus("OPEN");

        cashierSessionRepository.save(session);

        auditService.log(
                "CASHIER_SESSION",
                session.getId(),
                "OPEN_SESSION",
                null,
                session.getStatus(),
                "Daily cashier session opened"
        );

        return session;
    }

    @Transactional
    public CashierTransaction addManualTransaction(
            UUID sessionId,
            String paymentMethod,
            String direction,
            BigDecimal amount,
            String description
    ) {
        requireCashierPrivilege();

        CashierSession session = cashierSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier session not found"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalStateException("Manual transactions can only be added to open sessions.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        CashierTransaction transaction = new CashierTransaction();
        transaction.setCashierSession(session);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setDescription(description);

        cashierTransactionRepository.save(transaction);

        auditService.log(
                "CASHIER_SESSION",
                session.getId(),
                "ADD_MANUAL_TRANSACTION",
                null,
                amount.toPlainString(),
                description
        );

        return transaction;
    }

    @Transactional
    public CashierSession closeSession(UUID sessionId, BigDecimal actualClosingBalance, String explanation) {
        requireCashierPrivilege();

        CashierSession session = cashierSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier session not found"));

        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalStateException("Only open cashier sessions can be closed.");
        }

        BigDecimal expected = calculateExpectedClosingBalance(session);
        BigDecimal actual = actualClosingBalance != null ? actualClosingBalance : BigDecimal.ZERO;

        session.setExpectedClosingBalance(expected);
        session.setActualClosingBalance(actual);
        session.setVariance(actual.subtract(expected));
        session.setExplanation(explanation);
        session.setStatus("CLOSED");

        cashierSessionRepository.save(session);

        auditService.log(
                "CASHIER_SESSION",
                session.getId(),
                "CLOSE_SESSION",
                null,
                session.getStatus(),
                explanation
        );

        return session;
    }

    @Transactional
    public CashierSession approveSession(UUID sessionId) {
        requireCashierPrivilege();

        CashierSession session = cashierSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Cashier session not found"));

        if (!"CLOSED".equals(session.getStatus())) {
            throw new IllegalStateException("Only closed cashier sessions can be approved.");
        }

        session.setStatus("APPROVED");
        session.setApprovedBy(currentUserService.username());
        session.setApprovedAt(Instant.now());

        cashierSessionRepository.save(session);

        auditService.log(
                "CASHIER_SESSION",
                session.getId(),
                "APPROVE_SESSION",
                null,
                session.getStatus(),
                "Cashier session approved"
        );

        return session;
    }

    private BigDecimal calculateExpectedClosingBalance(CashierSession session) {
        List<Payment> payments = paymentRepository.findByDepartmentAndPaymentDate(
                session.getDepartment(),
                session.getSessionDate()
        );

        BigDecimal paymentIn = payments.stream()
                .filter(payment -> "IN".equals(payment.getDirection()))
                .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal paymentOut = payments.stream()
                .filter(payment -> "OUT".equals(payment.getDirection()))
                .map(payment -> payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CashierTransaction> manualTransactions = cashierTransactionRepository
                .findByCashierSessionId(session.getId());

        BigDecimal manualIn = manualTransactions.stream()
                .filter(transaction -> "IN".equals(transaction.getDirection()))
                .map(transaction -> transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal manualOut = manualTransactions.stream()
                .filter(transaction -> "OUT".equals(transaction.getDirection()))
                .map(transaction -> transaction.getAmount() != null ? transaction.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return nullSafe(session.getOpeningBalance())
                .add(paymentIn)
                .subtract(paymentOut)
                .add(manualIn)
                .subtract(manualOut);
    }

    private void requireCashierPrivilege() {
        if (!currentUserService.hasPrivilege("CASHIER_RECONCILE")) {
            throw new AccessDeniedException("Current user cannot perform cashier reconciliation.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}