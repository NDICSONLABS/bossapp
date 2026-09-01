// src/main/java/com/institution/finance/service/InternalBillingService.java
package cm.ndicsonlabs.bossapp.service.interdept;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalServiceCatalog;
import cm.ndicsonlabs.bossapp.domain.interdept.InternalSettlement;
import cm.ndicsonlabs.bossapp.repository.DepartmentRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalInvoiceRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalServiceCatalogRepository;
import cm.ndicsonlabs.bossapp.repository.interdept.InternalSettlementRepository;
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
public class InternalBillingService {

    private final InternalInvoiceRepository invoiceRepository;
    private final InternalServiceCatalogRepository serviceRepository;
    private final DepartmentRepository departmentRepository;
    private final InternalSettlementRepository settlementRepository;
    private final InternalBillingAccountingService accountingService;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    public InternalBillingService(
            InternalInvoiceRepository invoiceRepository,
            InternalServiceCatalogRepository serviceRepository,
            DepartmentRepository departmentRepository,
            InternalSettlementRepository settlementRepository,
            InternalBillingAccountingService accountingService,
            CurrentUserService currentUserService,
            AuditService auditService
    ) {
        this.invoiceRepository = invoiceRepository;
        this.serviceRepository = serviceRepository;
        this.departmentRepository = departmentRepository;
        this.settlementRepository = settlementRepository;
        this.accountingService = accountingService;
        this.currentUserService = currentUserService;
        this.auditService = auditService;
    }

    @Transactional
    public InternalInvoice createInvoice(
            UUID providerDepartmentId,
            UUID receiverDepartmentId,
            UUID serviceId,
            String description,
            BigDecimal amount,
            LocalDate transactionDate,
            LocalDate dueDate
    ) {
        requireInternalBillingPrivilege();

        if (providerDepartmentId.equals(receiverDepartmentId)) {
            throw new IllegalArgumentException("Provider and receiver departments must be different.");
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Internal invoice amount must be greater than zero.");
        }

        Department provider = departmentRepository.findById(providerDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Provider department not found"));

        Department receiver = departmentRepository.findById(receiverDepartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver department not found"));

        InternalServiceCatalog service = null;

        if (serviceId != null) {
            service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Internal service not found"));
        }

        InternalInvoice invoice = new InternalInvoice();
        invoice.setInvoiceNumber("INT-INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setProviderDepartment(provider);
        invoice.setReceiverDepartment(receiver);
        invoice.setService(service);
        invoice.setDescription(description);
        invoice.setTransactionDate(transactionDate);
        invoice.setDueDate(dueDate);
        invoice.setAmount(amount);
        invoice.setStatus("DRAFT");
        invoice.setCreatedBy(currentUserService.username());

        invoiceRepository.save(invoice);

        auditService.log(
                "INTERNAL_INVOICE",
                invoice.getId(),
                "CREATE_INTERNAL_INVOICE",
                null,
                invoice.getInvoiceNumber(),
                "Internal invoice created"
        );

        return invoice;
    }

    @Transactional
    public InternalInvoice postInvoice(UUID invoiceId) {
        requireInternalBillingPrivilege();

        InternalInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Internal invoice not found"));

        if (!"DRAFT".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only draft internal invoices can be posted.");
        }

        accountingService.postInternalInvoice(invoice);

        invoice.setStatus("POSTED");
        invoice.setPostedAt(Instant.now());

        invoiceRepository.save(invoice);

        auditService.log(
                "INTERNAL_INVOICE",
                invoice.getId(),
                "POST_INTERNAL_INVOICE",
                null,
                invoice.getInvoiceNumber(),
                "Internal invoice posted"
        );

        return invoice;
    }

    @Transactional
    public InternalSettlement settleInvoice(UUID invoiceId) {
        requireInternalBillingPrivilege();

        InternalInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Internal invoice not found"));

        if (!"POSTED".equals(invoice.getStatus())) {
            throw new IllegalStateException("Only posted internal invoices can be settled.");
        }

        InternalSettlement settlement = new InternalSettlement();
        settlement.setProviderDepartment(invoice.getProviderDepartment());
        settlement.setReceiverDepartment(invoice.getReceiverDepartment());
        settlement.setSettlementDate(LocalDate.now());
        settlement.setAmount(invoice.getAmount());
        settlement.setReference(invoice.getInvoiceNumber());
        settlement.setStatus("POSTED");
        settlement.setPostedBy(currentUserService.username());

        settlementRepository.save(settlement);

        accountingService.postInternalSettlement(settlement);

        invoice.setStatus("SETTLED");
        invoice.setSettlementDate(LocalDate.now());
        invoice.setSettledAt(Instant.now());

        invoiceRepository.save(invoice);

        auditService.log(
                "INTERNAL_INVOICE",
                invoice.getId(),
                "SETTLE_INTERNAL_INVOICE",
                null,
                settlement.getReference(),
                "Internal invoice settled"
        );

        return settlement;
    }

    public List<InternalInvoice> openInvoices() {
        return invoiceRepository.findByStatusOrderByCreatedAtDesc("POSTED");
    }

    private void requireInternalBillingPrivilege() {
        if (!currentUserService.hasPrivilege("INTERNAL_BILLING_MANAGE")) {
            throw new AccessDeniedException("Current user does not have internal billing privilege.");
        }
    }
}