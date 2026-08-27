// src/main/java/com/institution/finance/service/ProcurementControlService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.PurchaseOrder;
import cm.ndicsonlabs.bossapp.domain.Supplier;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import cm.ndicsonlabs.bossapp.domain.SupplierStatementReconciliation;
import cm.ndicsonlabs.bossapp.dto.SupplierBalanceLine;
import cm.ndicsonlabs.bossapp.repository.PurchaseOrderRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierInvoiceRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierStatementReconciliationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProcurementControlService {

    private final SupplierRepository supplierRepository;
    private final SupplierInvoiceRepository supplierInvoiceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierStatementReconciliationRepository reconciliationRepository;

    public ProcurementControlService(
            SupplierRepository supplierRepository,
            SupplierInvoiceRepository supplierInvoiceRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            SupplierStatementReconciliationRepository reconciliationRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.supplierInvoiceRepository = supplierInvoiceRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.reconciliationRepository = reconciliationRepository;
    }

    public List<SupplierBalanceLine> supplierBalances() {
        List<SupplierBalanceLine> lines = new ArrayList<>();

        for (Supplier supplier : supplierRepository.findAll()) {
            List<SupplierInvoice> invoices = supplierInvoiceRepository.findBySupplierId(supplier.getId());

            BigDecimal totalInvoiced = invoices.stream()
                    .map(invoice -> nullSafe(invoice.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalPaid = invoices.stream()
                    .map(invoice -> nullSafe(invoice.getPaidAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal outstanding = invoices.stream()
                    .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal overdue = invoices.stream()
                    .filter(invoice -> invoice.getDueDate() != null)
                    .filter(invoice -> invoice.getDueDate().isBefore(LocalDate.now()))
                    .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lines.add(new SupplierBalanceLine(
                    supplier.getCode(),
                    supplier.getName(),
                    supplier.getCategory(),
                    totalInvoiced,
                    totalPaid,
                    outstanding,
                    overdue
            ));
        }

        return lines.stream()
                .sorted(Comparator.comparing(SupplierBalanceLine::getSupplierCode))
                .toList();
    }

    public List<PurchaseOrder> openCommitments() {
        return purchaseOrderRepository.findByStatusIn(
                List.of("APPROVED", "PARTIALLY_RECEIVED")
        );
    }

    public List<SupplierInvoice> paymentForecast(int days) {
        LocalDate limit = LocalDate.now().plusDays(days);

        return supplierInvoiceRepository.findAll()
                .stream()
                .filter(invoice -> invoice.getRemainingAmount() != null)
                .filter(invoice -> invoice.getRemainingAmount().signum() > 0)
                .filter(invoice -> invoice.getDueDate() != null)
                .filter(invoice -> !invoice.getDueDate().isAfter(limit))
                .sorted(Comparator.comparing(SupplierInvoice::getDueDate))
                .toList();
    }

    @Transactional
    public SupplierStatementReconciliation reconcileSupplierStatement(
            UUID supplierId,
            LocalDate statementDate,
            BigDecimal supplierBalance,
            String notes
    ) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

        BigDecimal systemBalance = supplierInvoiceRepository.findBySupplierId(supplierId)
                .stream()
                .filter(invoice -> invoice.getInvoiceDate() == null || !invoice.getInvoiceDate().isAfter(statementDate))
                .map(invoice -> nullSafe(invoice.getRemainingAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SupplierStatementReconciliation reconciliation = new SupplierStatementReconciliation();
        reconciliation.setSupplier(supplier);
        reconciliation.setStatementDate(statementDate);
        reconciliation.setSupplierBalance(nullSafe(supplierBalance));
        reconciliation.setSystemBalance(systemBalance);
        reconciliation.setVariance(nullSafe(supplierBalance).subtract(systemBalance));
        reconciliation.setStatus(reconciliation.getVariance().compareTo(BigDecimal.ZERO) == 0 ? "BALANCED" : "VARIANCE");
        reconciliation.setNotes(notes);

        return reconciliationRepository.save(reconciliation);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}