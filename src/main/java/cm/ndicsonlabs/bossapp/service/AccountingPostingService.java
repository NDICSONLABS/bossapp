// src/main/java/com/institution/finance/service/AccountingPostingService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AccountCode;
import cm.ndicsonlabs.bossapp.domain.AccountMapping;
import cm.ndicsonlabs.bossapp.domain.AccountingEntry;
import cm.ndicsonlabs.bossapp.domain.AccountingEntryLine;
import cm.ndicsonlabs.bossapp.domain.AccountingPeriod;
import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import cm.ndicsonlabs.bossapp.domain.Payment;
import cm.ndicsonlabs.bossapp.domain.StudentCharge;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.AccountCodeRepository;
import cm.ndicsonlabs.bossapp.repository.AccountMappingRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryLineRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingEntryRepository;
import cm.ndicsonlabs.bossapp.repository.AccountingPeriodRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.PaymentRepository;
import cm.ndicsonlabs.bossapp.repository.StudentChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
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
public class AccountingPostingService {

    private final AccountCodeRepository accountCodeRepository;
    private final AccountMappingRepository accountMappingRepository;
    private final AccountingEntryRepository entryRepository;
    private final AccountingEntryLineRepository lineRepository;
    private final AccountingPeriodRepository periodRepository;
    private final StudentChargeRepository studentChargeRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public AccountingPostingService(
            AccountCodeRepository accountCodeRepository,
            AccountMappingRepository accountMappingRepository,
            AccountingEntryRepository entryRepository,
            AccountingEntryLineRepository lineRepository,
            AccountingPeriodRepository periodRepository,
            StudentChargeRepository studentChargeRepository,
            PatientChargeRepository patientChargeRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PaymentRepository paymentRepository,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.accountCodeRepository = accountCodeRepository;
        this.accountMappingRepository = accountMappingRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.periodRepository = periodRepository;
        this.studentChargeRepository = studentChargeRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountingEntry postStudentCharge(UUID studentChargeId) {
        requireNotPosted("STUDENT_CHARGE", studentChargeId);

        StudentCharge charge = studentChargeRepository.findById(studentChargeId)
                .orElseThrow(() -> new IllegalArgumentException("Student charge not found"));

        BigDecimal amount = charge.getNetAmount() != null ? charge.getNetAmount() : charge.getAmount();

        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Student charge has no postable amount.");
        }

        LocalDate entryDate = charge.getChargeDate() != null ? charge.getChargeDate() : LocalDate.now();

        AccountingEntry entry = prepareEntry(
                charge.getDepartment(),
                entryDate,
                "STUDENT_CHARGE",
                charge.getId(),
                null,
                "Student charge posting for charge " + charge.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(createLine(
                entry,
                getMappedAccount("STUDENT_CHARGE_AR"),
                amount,
                BigDecimal.ZERO,
                charge.getDepartment(),
                "Student receivable"
        ));

        lines.add(createLine(
                entry,
                getMappedAccount("STUDENT_CHARGE_REVENUE"),
                BigDecimal.ZERO,
                amount,
                charge.getDepartment(),
                "Education revenue"
        ));

        savePostedEntry(entry, lines);

        auditService.log(
                "STUDENT_CHARGE",
                charge.getId(),
                "POST_TO_ACCOUNTING",
                null,
                entry.getEntryNumber(),
                "Student charge posted to double-entry accounting"
        );

        return entry;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountingEntry postPatientCharge(UUID patientChargeId) {
        requireNotPosted("PATIENT_CHARGE", patientChargeId);

        PatientCharge charge = patientChargeRepository.findById(patientChargeId)
                .orElseThrow(() -> new IllegalArgumentException("Patient charge not found"));

        BigDecimal amount = charge.getAmount();

        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Patient charge has no postable amount.");
        }

        LocalDate entryDate = charge.getChargeDate() != null ? charge.getChargeDate() : LocalDate.now();

        AccountingEntry entry = prepareEntry(
                charge.getDepartment(),
                entryDate,
                "PATIENT_CHARGE",
                charge.getId(),
                null,
                "Patient charge posting for charge " + charge.getId()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(createLine(
                entry,
                getMappedAccount("PATIENT_CHARGE_AR"),
                amount,
                BigDecimal.ZERO,
                charge.getDepartment(),
                "Patient receivable"
        ));

        lines.add(createLine(
                entry,
                getMappedAccount("PATIENT_CHARGE_REVENUE"),
                BigDecimal.ZERO,
                amount,
                charge.getDepartment(),
                "Health services revenue"
        ));

        savePostedEntry(entry, lines);

        auditService.log(
                "PATIENT_CHARGE",
                charge.getId(),
                "POST_TO_ACCOUNTING",
                null,
                entry.getEntryNumber(),
                "Patient charge posted to double-entry accounting"
        );

        return entry;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountingEntry postSupplierInvoice(UUID supplierInvoiceId) {
        requireNotPosted("SUPPLIER_INVOICE", supplierInvoiceId);

        SupplierInvoice invoice = supplierInvoiceRepository.findById(supplierInvoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier invoice not found"));

        BigDecimal amount = invoice.getTotalAmount();

        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Supplier invoice has no postable amount.");
        }

        LocalDate entryDate = invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : LocalDate.now();

        AccountingEntry entry = prepareEntry(
                invoice.getDepartment(),
                entryDate,
                "SUPPLIER_INVOICE",
                invoice.getId(),
                null,
                "Supplier invoice posting for invoice " + invoice.getInvoiceNumber()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        lines.add(createLine(
                entry,
                getMappedAccount("SUPPLIER_INVOICE_EXPENSE"),
                amount,
                BigDecimal.ZERO,
                invoice.getDepartment(),
                "Supplier expense"
        ));

        lines.add(createLine(
                entry,
                getMappedAccount("SUPPLIER_INVOICE_AP"),
                BigDecimal.ZERO,
                amount,
                invoice.getDepartment(),
                "Supplier payable"
        ));

        savePostedEntry(entry, lines);

        auditService.log(
                "SUPPLIER_INVOICE",
                invoice.getId(),
                "POST_TO_ACCOUNTING",
                null,
                entry.getEntryNumber(),
                "Supplier invoice posted to double-entry accounting"
        );

        return entry;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountingEntry postPayment(UUID paymentId) {
        requireNotPosted("PAYMENT", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        BigDecimal amount = payment.getAmount();

        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Payment has no postable amount.");
        }

        if (payment.getDirection() == null) {
            throw new IllegalArgumentException("Payment direction is required for posting.");
        }

        LocalDate entryDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();

        AccountingEntry entry = prepareEntry(
                payment.getDepartment(),
                entryDate,
                "PAYMENT",
                payment.getId(),
                null,
                "Payment posting for payment " + payment.getPaymentNumber()
        );

        List<AccountingEntryLine> lines = new ArrayList<>();

        if ("IN".equals(payment.getDirection())) {
            lines.add(createLine(
                    entry,
                    getMappedAccount("PAYMENT_IN_CASH"),
                    amount,
                    BigDecimal.ZERO,
                    payment.getDepartment(),
                    "Cash received"
            ));

            lines.add(createLine(
                    entry,
                    getMappedAccount("PAYMENT_IN_AR"),
                    BigDecimal.ZERO,
                    amount,
                    payment.getDepartment(),
                    "Receivable settlement"
            ));
        } else if ("OUT".equals(payment.getDirection())) {
            lines.add(createLine(
                    entry,
                    getMappedAccount("PAYMENT_OUT_AP"),
                    amount,
                    BigDecimal.ZERO,
                    payment.getDepartment(),
                    "Payable settlement"
            ));

            lines.add(createLine(
                    entry,
                    getMappedAccount("PAYMENT_OUT_CASH"),
                    BigDecimal.ZERO,
                    amount,
                    payment.getDepartment(),
                    "Cash paid"
            ));
        } else {
            throw new IllegalArgumentException("Unsupported payment direction: " + payment.getDirection());
        }

        savePostedEntry(entry, lines);

        auditService.log(
                "PAYMENT",
                payment.getId(),
                "POST_TO_ACCOUNTING",
                null,
                entry.getEntryNumber(),
                "Payment posted to double-entry accounting"
        );

        return entry;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AccountingEntry reverseEntry(UUID entryId, String reason) {
        AccountingEntry original = entryRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Accounting entry not found"));

        if (!"POSTED".equals(original.getStatus())) {
            throw new IllegalStateException("Only posted entries can be reversed.");
        }

        List<AccountingEntryLine> originalLines = lineRepository.findByEntryId(original.getId());

        if (originalLines.isEmpty()) {
            throw new IllegalStateException("Accounting entry has no lines.");
        }

        AccountingEntry reversal = prepareEntry(
                original.getDepartment(),
                LocalDate.now(),
                "REVERSAL",
                original.getId(),
                original.getId(),
                "Reversal of " + original.getEntryNumber() + ": " + reason
        );

        List<AccountingEntryLine> reversalLines = new ArrayList<>();

        for (AccountingEntryLine originalLine : originalLines) {
            reversalLines.add(createLine(
                    reversal,
                    originalLine.getAccountCode(),
                    originalLine.getCredit(),
                    originalLine.getDebit(),
                    originalLine.getDepartment(),
                    "Reversal: " + originalLine.getDescription()
            ));
        }

        savePostedEntry(reversal, reversalLines);

        original.setStatus("REVERSED");
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

    private AccountingEntry prepareEntry(
            Department department,
            LocalDate entryDate,
            String sourceType,
            UUID sourceId,
            UUID originalEntryId,
            String description
    ) {
        AccountingPeriod period = getOpenPeriodForDate(entryDate);

        AccountingEntry entry = new AccountingEntry();
        entry.setEntryNumber("JE-" + UUID.randomUUID());
        entry.setEntryDate(entryDate);
        entry.setAccountingPeriod(period);
        entry.setDepartment(department);
        entry.setDescription(description);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setOriginalEntryId(originalEntryId);
        entry.setStatus("POSTED");
        entry.setPostedBy(currentUserService.username());
        entry.setPostedAt(Instant.now());

        return entry;
    }

    private AccountingEntryLine createLine(
            AccountingEntry entry,
            AccountCode accountCode,
            BigDecimal debit,
            BigDecimal credit,
            Department department,
            String description
    ) {
        AccountingEntryLine line = new AccountingEntryLine();
        line.setEntry(entry);
        line.setAccountCode(accountCode);
        line.setDebit(debit != null ? debit : BigDecimal.ZERO);
        line.setCredit(credit != null ? credit : BigDecimal.ZERO);
        line.setDepartment(department);
        line.setDescription(description);

        return line;
    }

    private void savePostedEntry(AccountingEntry entry, List<AccountingEntryLine> lines) {
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

        for (AccountingEntryLine line : lines) {
            BigDecimal debit = line.getDebit() != null ? line.getDebit() : BigDecimal.ZERO;
            BigDecimal credit = line.getCredit() != null ? line.getCredit() : BigDecimal.ZERO;

            if (debit.signum() < 0 || credit.signum() < 0) {
                throw new IllegalStateException("Accounting entry lines cannot contain negative amounts.");
            }

            if (debit.signum() > 0 && credit.signum() > 0) {
                throw new IllegalStateException("An accounting entry line cannot have both debit and credit amounts.");
            }
        }

        entryRepository.save(entry);
        lineRepository.saveAll(lines);
    }

    private AccountCode getMappedAccount(String mappingType) {
        AccountMapping mapping = accountMappingRepository.findByMappingType(mappingType)
                .orElseThrow(() -> new IllegalStateException("Account mapping not found: " + mappingType));

        if (!mapping.isActive()) {
            throw new IllegalStateException("Account mapping is inactive: " + mappingType);
        }

        return mapping.getAccountCode();
    }

    private AccountingPeriod getOpenPeriodForDate(LocalDate date) {
        AccountingPeriod period = periodRepository
                .findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(date, date)
                .orElseThrow(() -> new IllegalStateException("No accounting period found for date: " + date));

        if (!"OPEN".equals(period.getStatus())) {
            throw new IllegalStateException("Accounting period is not open for date: " + date);
        }

        return period;
    }

    private void requireNotPosted(String sourceType, UUID sourceId) {
        if (entryRepository.existsBySourceTypeAndSourceId(sourceType, sourceId)) {
            throw new IllegalStateException("This transaction has already been posted to accounting.");
        }
    }
}