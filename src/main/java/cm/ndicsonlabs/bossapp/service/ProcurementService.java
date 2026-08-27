// src/main/java/com/institution/finance/service/ProcurementService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.GoodsReceipt;
import cm.ndicsonlabs.bossapp.domain.GoodsReceiptLine;
import cm.ndicsonlabs.bossapp.domain.ProcurementMatchIssue;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.PurchaseOrderLine;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.repository.GoodsReceiptLineRepository;
import cm.ndicsonlabs.bossapp.repository.GoodsReceiptRepository;
import cm.ndicsonlabs.bossapp.repository.ProcurementMatchIssueRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderLineRepository;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProcurementService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptLineRepository goodsReceiptLineRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final ProcurementMatchIssueRepository matchIssueRepository;
    private final CurrentUserService currentUserService;
    private final SupplierCreditService supplierCreditService;

    public ProcurementService(
            PurchaseOrderRepository purchaseOrderRepository,
            PurchaseOrderLineRepository purchaseOrderLineRepository,
            GoodsReceiptRepository goodsReceiptRepository,
            GoodsReceiptLineRepository goodsReceiptLineRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            ProcurementMatchIssueRepository matchIssueRepository,
            CurrentUserService currentUserService, SupplierCreditService supplierCreditService
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.purchaseOrderLineRepository = purchaseOrderLineRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.goodsReceiptLineRepository = goodsReceiptLineRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.matchIssueRepository = matchIssueRepository;
        this.currentUserService = currentUserService;
        this.supplierCreditService = supplierCreditService;
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(
            Supplier supplier,
            Department department,
            LocalDate expectedDeliveryDate,
            String currency,
            List<NewPurchaseOrderLine> lines
    ) {
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("At least one purchase order line is required.");
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setPoNumber("PO-" + UUID.randomUUID());
        order.setSupplier(supplier);
        order.setDepartment(department);
        order.setOrderDate(LocalDate.now());
        order.setExpectedDeliveryDate(expectedDeliveryDate);
        order.setCurrency(currency);
        order.setStatus("APPROVED");
        order.setApprovedBy(currentUserService.username());
        order.setApprovedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;

        purchaseOrderRepository.save(order);

        for (NewPurchaseOrderLine newLine : lines) {
            BigDecimal lineTotal = calculateLineTotal(
                    newLine.quantity(),
                    newLine.unitPrice(),
                    newLine.taxPercent()
            );

            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setPurchaseOrder(order);
            line.setDescription(newLine.description());
            line.setQuantity(newLine.quantity());
            line.setUnitPrice(newLine.unitPrice());
            line.setTaxPercent(newLine.taxPercent() != null ? newLine.taxPercent() : BigDecimal.ZERO);
            line.setLineTotal(lineTotal);
            line.setReceivedQuantity(BigDecimal.ZERO);
            line.setAcceptedQuantity(BigDecimal.ZERO);

            purchaseOrderLineRepository.save(line);

            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public GoodsReceipt receiveRemainingQuantities(UUID purchaseOrderId, LocalDate deliveryDate) {
        PurchaseOrder order = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));

        if ("CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Cannot receive goods against a cancelled purchase order.");
        }

        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setGrnNumber("GRN-" + UUID.randomUUID());
        receipt.setSupplier(order.getSupplier());
        receipt.setDepartment(order.getDepartment());
        receipt.setPurchaseOrder(order);
        receipt.setDeliveryDate(deliveryDate != null ? deliveryDate : LocalDate.now());
        receipt.setReceivedBy(currentUserService.username());
        receipt.setStatus("RECEIVED");

        goodsReceiptRepository.save(receipt);

        boolean anyReceived = false;
        boolean fullyReceived = true;

        for (PurchaseOrderLine line : lines) {
            BigDecimal ordered = nullSafe(line.getQuantity());
            BigDecimal received = nullSafe(line.getReceivedQuantity());
            BigDecimal remaining = ordered.subtract(received);

            if (remaining.signum() > 0) {
                anyReceived = true;

                GoodsReceiptLine receiptLine = new GoodsReceiptLine();
                receiptLine.setGoodsReceipt(receipt);
                receiptLine.setPurchaseOrderLine(line);
                receiptLine.setQuantityOrdered(ordered);
                receiptLine.setQuantityReceived(remaining);
                receiptLine.setAcceptedQuantity(remaining);
                receiptLine.setRejectedQuantity(BigDecimal.ZERO);

                goodsReceiptLineRepository.save(receiptLine);

                line.setReceivedQuantity(received.add(remaining));
                line.setAcceptedQuantity(nullSafe(line.getAcceptedQuantity()).add(remaining));
                purchaseOrderLineRepository.save(line);
            }

            if (nullSafe(line.getReceivedQuantity()).compareTo(ordered) < 0) {
                fullyReceived = false;
            }
        }

        if (!anyReceived) {
            throw new IllegalStateException("No remaining quantities to receive for this purchase order.");
        }

        order.setStatus(fullyReceived ? "RECEIVED" : "PARTIALLY_RECEIVED");
        purchaseOrderRepository.save(order);

        return receipt;
    }

    @Transactional
    public SupplierInvoice createInvoiceFromPurchaseOrder(
            UUID purchaseOrderId,
            String invoiceNumber,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal totalAmount
    ) {
        PurchaseOrder order = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Purchase order not found"));

        supplierCreditService.validateSupplierCredit(order.getSupplier().getId(), totalAmount);

        if (supplierInvoiceRepository.existsBySupplierIdAndInvoiceNumber(order.getSupplier().getId(), invoiceNumber)) {
            throw new IllegalStateException("Duplicate supplier invoice number detected: " + invoiceNumber);
        }

        GoodsReceipt receipt = goodsReceiptRepository
                .findFirstByPurchaseOrderIdOrderByCreatedAtDesc(order.getId())
                .orElse(null);

        SupplierInvoice invoice = new SupplierInvoice();
        invoice.setSupplier(order.getSupplier());
        invoice.setDepartment(order.getDepartment());
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setInvoiceDate(invoiceDate != null ? invoiceDate : LocalDate.now());
        invoice.setDueDate(calculateDueDate(order.getSupplier(), invoice.getInvoiceDate(), dueDate));
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setStatus("POSTED");
        invoice.setAccountingStatus("NOT_SUBMITTED");
        invoice.setPurchaseOrder(order);
        invoice.setGoodsReceipt(receipt);
        invoice.setMatchStatus("UNMATCHED");

        supplierInvoiceRepository.save(invoice);

        matchInvoice(invoice.getId());

        return invoice;
    }

    @Transactional
    public SupplierInvoice matchInvoice(UUID supplierInvoiceId) {
        SupplierInvoice invoice = supplierInvoiceRepository.findById(supplierInvoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier invoice not found"));

        matchIssueRepository.deleteBySupplierInvoiceId(invoice.getId());

        List<ProcurementMatchIssue> issues = new ArrayList<>();

        if (invoice.getPurchaseOrder() == null) {
            addIssue(
                    issues,
                    invoice,
                    null,
                    null,
                    "MISSING_PO",
                    "ERROR",
                    "Supplier invoice is not linked to a purchase order.",
                    null
            );

            invoice.setMatchStatus("DISPUTED");
            supplierInvoiceRepository.save(invoice);
            matchIssueRepository.saveAll(issues);

            return invoice;
        }

        PurchaseOrder order = invoice.getPurchaseOrder();

        GoodsReceipt receipt = invoice.getGoodsReceipt() != null
                ? invoice.getGoodsReceipt()
                : goodsReceiptRepository.findFirstByPurchaseOrderIdOrderByCreatedAtDesc(order.getId()).orElse(null);

        if (receipt == null) {
            addIssue(
                    issues,
                    invoice,
                    order,
                    null,
                    "MISSING_GRN",
                    "ERROR",
                    "Supplier invoice has no linked goods receipt.",
                    null
            );
        }

        BigDecimal poTotal = nullSafe(order.getTotalAmount());
        BigDecimal acceptedValue = calculateAcceptedValue(order);
        BigDecimal invoiceTotal = nullSafe(invoice.getTotalAmount());

        if (invoiceTotal.compareTo(poTotal) > 0) {
            addIssue(
                    issues,
                    invoice,
                    order,
                    receipt,
                    "INVOICE_EXCEEDS_PO",
                    "ERROR",
                    "Invoice amount exceeds purchase order amount.",
                    invoiceTotal.subtract(poTotal)
            );
        }

        if (receipt != null && invoiceTotal.compareTo(acceptedValue) > 0) {
            addIssue(
                    issues,
                    invoice,
                    order,
                    receipt,
                    "INVOICE_EXCEEDS_RECEIPT",
                    "ERROR",
                    "Invoice amount exceeds accepted goods receipt value.",
                    invoiceTotal.subtract(acceptedValue)
            );
        }

        if (receipt != null && invoiceTotal.compareTo(acceptedValue) < 0) {
            addIssue(
                    issues,
                    invoice,
                    order,
                    receipt,
                    "PARTIAL_BILLING",
                    "WARNING",
                    "Invoice amount is less than accepted goods receipt value.",
                    acceptedValue.subtract(invoiceTotal)
            );
        }

        boolean hasError = issues.stream()
                .anyMatch(issue -> "ERROR".equals(issue.getSeverity()));

        invoice.setMatchStatus(hasError ? "DISPUTED" : "MATCHED");
        invoice.setGoodsReceipt(receipt);

        supplierInvoiceRepository.save(invoice);
        matchIssueRepository.saveAll(issues);

        return invoice;
    }

    private BigDecimal calculateAcceptedValue(PurchaseOrder order) {
        List<PurchaseOrderLine> lines = purchaseOrderLineRepository.findByPurchaseOrderId(order.getId());

        BigDecimal acceptedValue = BigDecimal.ZERO;

        for (PurchaseOrderLine line : lines) {
            BigDecimal ordered = nullSafe(line.getQuantity());
            BigDecimal accepted = nullSafe(line.getAcceptedQuantity());
            BigDecimal lineTotal = nullSafe(line.getLineTotal());

            if (ordered.signum() > 0) {
                acceptedValue = acceptedValue.add(
                        lineTotal.multiply(accepted).divide(ordered, 4, RoundingMode.HALF_UP)
                );
            }
        }

        return acceptedValue;
    }

    private BigDecimal calculateLineTotal(BigDecimal quantity, BigDecimal unitPrice, BigDecimal taxPercent) {
        BigDecimal qty = nullSafe(quantity);
        BigDecimal price = nullSafe(unitPrice);
        BigDecimal tax = nullSafe(taxPercent);

        BigDecimal gross = qty.multiply(price);

        BigDecimal multiplier = BigDecimal.ONE.add(
                tax.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );

        return gross.multiply(multiplier).setScale(4, RoundingMode.HALF_UP);
    }

    private LocalDate calculateDueDate(Supplier supplier, LocalDate invoiceDate, LocalDate explicitDueDate) {
        if (explicitDueDate != null) {
            return explicitDueDate;
        }

        int terms = supplier.getPaymentTermsDays() != null ? supplier.getPaymentTermsDays() : 30;

        return invoiceDate.plusDays(terms);
    }

    private void addIssue(
            List<ProcurementMatchIssue> issues,
            SupplierInvoice invoice,
            PurchaseOrder order,
            GoodsReceipt receipt,
            String issueType,
            String severity,
            String message,
            BigDecimal amount
    ) {
        ProcurementMatchIssue issue = new ProcurementMatchIssue();
        issue.setSupplierInvoice(invoice);
        issue.setPurchaseOrder(order);
        issue.setGoodsReceipt(receipt);
        issue.setIssueType(issueType);
        issue.setSeverity(severity);
        issue.setMessage(message);
        issue.setAmount(amount);

        issues.add(issue);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    public record NewPurchaseOrderLine(
            String description,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal taxPercent
    ) {
    }
}