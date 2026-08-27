// src/main/java/com/institution/finance/service/InventoryService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.InventoryBalance;
import cm.ndicsonlabs.bossapp.domain.InventoryItem;
import cm.ndicsonlabs.bossapp.domain.InventoryLocation;
import cm.ndicsonlabs.bossapp.domain.InventoryTransaction;
import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import cm.ndicsonlabs.bossapp.domain.PatientCharge;
import cm.ndicsonlabs.bossapp.domain.SupplierBatch;
import cm.ndicsonlabs.bossapp.dto.InventoryBalanceLine;
import cm.ndicsonlabs.bossapp.repository.InventoryBalanceRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryItemRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryLocationRepository;
import cm.ndicsonlabs.bossapp.repository.InventoryTransactionRepository;
import cm.ndicsonlabs.bossapp.repository.PatientAccountRepository;
import cm.ndicsonlabs.bossapp.repository.PatientChargeRepository;
import cm.ndicsonlabs.bossapp.repository.SupplierBatchRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryLocationRepository locationRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final SupplierBatchRepository batchRepository;
    private final PatientAccountRepository patientAccountRepository;
    private final PatientChargeRepository patientChargeRepository;
    private final InventoryAccountingService inventoryAccountingService;
    private final GlIntegrationService glIntegrationService;
    private final CurrentUserService currentUserService;

    public InventoryService(
            InventoryItemRepository itemRepository,
            InventoryLocationRepository locationRepository,
            InventoryBalanceRepository balanceRepository,
            InventoryTransactionRepository transactionRepository,
            SupplierBatchRepository batchRepository,
            PatientAccountRepository patientAccountRepository,
            PatientChargeRepository patientChargeRepository,
            InventoryAccountingService inventoryAccountingService,
            GlIntegrationService glIntegrationService,
            CurrentUserService currentUserService
    ) {
        this.itemRepository = itemRepository;
        this.locationRepository = locationRepository;
        this.balanceRepository = balanceRepository;
        this.transactionRepository = transactionRepository;
        this.batchRepository = batchRepository;
        this.patientAccountRepository = patientAccountRepository;
        this.patientChargeRepository = patientChargeRepository;
        this.inventoryAccountingService = inventoryAccountingService;
        this.glIntegrationService = glIntegrationService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public InventoryTransaction receiveStock(
            UUID locationId,
            UUID itemId,
            String batchNumber,
            LocalDate expiryDate,
            BigDecimal quantity,
            BigDecimal unitCost,
            String referenceType,
            UUID referenceId,
            String notes
    ) {
        InventoryLocation location = getLocation(locationId);
        InventoryItem item = getItem(itemId);

        validatePositiveQuantity(quantity);

        SupplierBatch batch = resolveBatch(location, item, batchNumber, expiryDate, unitCost);

        InventoryBalance balance = findOrCreateBalance(item, location, batch);

        BigDecimal existingQuantity = nullSafe(balance.getQuantityOnHand());
        BigDecimal existingCost = nullSafe(balance.getAverageCost());
        BigDecimal incomingQuantity = nullSafe(quantity);
        BigDecimal incomingCost = nullSafe(unitCost);

        BigDecimal newQuantity = existingQuantity.add(incomingQuantity);

        BigDecimal newAverageCost = BigDecimal.ZERO;

        if (newQuantity.signum() > 0) {
            newAverageCost = existingQuantity.multiply(existingCost)
                    .add(incomingQuantity.multiply(incomingCost))
                    .divide(newQuantity, 4, RoundingMode.HALF_UP);
        }

        balance.setQuantityOnHand(newQuantity);
        balance.setAverageCost(newAverageCost);
        balanceRepository.save(balance);

        InventoryTransaction transaction = createTransaction(
                item,
                location,
                batch,
                "RECEIPT",
                incomingQuantity,
                incomingCost,
                referenceType,
                referenceId,
                notes
        );

        try {
            inventoryAccountingService.postReceipt(transaction);
            transaction.setStatus("POSTED");
        } catch (Exception ex) {
            transaction.setStatus("GL_ERROR");
            transaction.setNotes(appendNote(transaction.getNotes(), ex.getMessage()));
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public InventoryTransaction issueStock(
            UUID locationId,
            UUID itemId,
            String batchNumber,
            BigDecimal quantity,
            String referenceType,
            UUID referenceId,
            String notes
    ) {
        InventoryLocation location = getLocation(locationId);
        InventoryItem item = getItem(itemId);

        validatePositiveQuantity(quantity);

        SupplierBatch batch = findBatch(item, batchNumber);
        InventoryBalance balance = findBalance(item, location, batch);

        if (balance == null || nullSafe(balance.getQuantityOnHand()).compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient stock balance.");
        }

        BigDecimal cost = nullSafe(balance.getAverageCost());

        balance.setQuantityOnHand(balance.getQuantityOnHand().subtract(quantity));
        balanceRepository.save(balance);

        InventoryTransaction transaction = createTransaction(
                item,
                location,
                batch,
                "ISSUE",
                quantity,
                cost,
                referenceType,
                referenceId,
                notes
        );

        try {
            inventoryAccountingService.postIssue(transaction, false);
            transaction.setStatus("POSTED");
        } catch (Exception ex) {
            transaction.setStatus("GL_ERROR");
            transaction.setNotes(appendNote(transaction.getNotes(), ex.getMessage()));
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void transferStock(
            UUID fromLocationId,
            UUID toLocationId,
            UUID itemId,
            String batchNumber,
            BigDecimal quantity,
            String notes
    ) {
        if (fromLocationId.equals(toLocationId)) {
            throw new IllegalArgumentException("Source and destination locations must be different.");
        }

        InventoryLocation fromLocation = getLocation(fromLocationId);
        InventoryLocation toLocation = getLocation(toLocationId);
        InventoryItem item = getItem(itemId);

        validatePositiveQuantity(quantity);

        SupplierBatch batch = findBatch(item, batchNumber);
        InventoryBalance sourceBalance = findBalance(item, fromLocation, batch);

        if (sourceBalance == null || nullSafe(sourceBalance.getQuantityOnHand()).compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient stock balance for transfer.");
        }

        BigDecimal cost = nullSafe(sourceBalance.getAverageCost());

        sourceBalance.setQuantityOnHand(sourceBalance.getQuantityOnHand().subtract(quantity));
        balanceRepository.save(sourceBalance);

        InventoryBalance destinationBalance = findOrCreateBalance(item, toLocation, batch);
        destinationBalance.setQuantityOnHand(destinationBalance.getQuantityOnHand().add(quantity));
        destinationBalance.setAverageCost(cost);
        balanceRepository.save(destinationBalance);

        createTransaction(
                item,
                fromLocation,
                batch,
                "TRANSFER_OUT",
                quantity,
                cost,
                "TRANSFER",
                toLocationId,
                notes
        );

        createTransaction(
                item,
                toLocation,
                batch,
                "TRANSFER_IN",
                quantity,
                cost,
                "TRANSFER",
                fromLocationId,
                notes
        );
    }

    @Transactional
    public InventoryTransaction adjustStock(
            UUID locationId,
            UUID itemId,
            String batchNumber,
            BigDecimal signedQuantity,
            boolean writeOff,
            String notes
    ) {
        InventoryLocation location = getLocation(locationId);
        InventoryItem item = getItem(itemId);

        if (signedQuantity == null || signedQuantity.signum() == 0) {
            throw new IllegalArgumentException("Adjustment quantity cannot be zero.");
        }

        SupplierBatch batch = findBatch(item, batchNumber);
        InventoryBalance balance = findBalance(item, location, batch);

        BigDecimal cost = balance != null ? nullSafe(balance.getAverageCost()) : BigDecimal.ZERO;

        if (signedQuantity.signum() > 0) {
            InventoryBalance target = findOrCreateBalance(item, location, batch);
            target.setQuantityOnHand(nullSafe(target.getQuantityOnHand()).add(signedQuantity));
            balanceRepository.save(target);

            return createTransaction(
                    item,
                    location,
                    batch,
                    "ADJUSTMENT_IN",
                    signedQuantity,
                    cost,
                    "ADJUSTMENT",
                    null,
                    notes
            );
        }

        BigDecimal reduction = signedQuantity.abs();

        if (balance == null || nullSafe(balance.getQuantityOnHand()).compareTo(reduction) < 0) {
            throw new IllegalStateException("Insufficient stock balance for adjustment.");
        }

        balance.setQuantityOnHand(balance.getQuantityOnHand().subtract(reduction));
        balanceRepository.save(balance);

        InventoryTransaction transaction = createTransaction(
                item,
                location,
                batch,
                writeOff ? "WRITE_OFF" : "ADJUSTMENT_OUT",
                reduction,
                cost,
                writeOff ? "WRITE_OFF" : "ADJUSTMENT",
                null,
                notes
        );

        try {
            inventoryAccountingService.postIssue(transaction, writeOff);
            transaction.setStatus("POSTED");
        } catch (Exception ex) {
            transaction.setStatus("GL_ERROR");
            transaction.setNotes(appendNote(transaction.getNotes(), ex.getMessage()));
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public PatientCharge issueToPatient(
            UUID locationId,
            UUID itemId,
            String batchNumber,
            BigDecimal quantity,
            UUID patientAccountId,
            String notes
    ) {
        InventoryLocation location = getLocation(locationId);
        InventoryItem item = getItem(itemId);
        PatientAccount patientAccount = patientAccountRepository.findById(patientAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Patient account not found"));

        validatePositiveQuantity(quantity);

        SupplierBatch batch = findBatch(item, batchNumber);
        InventoryBalance balance = findBalance(item, location, batch);

        if (balance == null || nullSafe(balance.getQuantityOnHand()).compareTo(quantity) < 0) {
            throw new IllegalStateException("Insufficient stock balance for patient issue.");
        }

        BigDecimal billingPrice = item.getSalePrice() != null
                ? item.getSalePrice()
                : item.getStandardCost() != null
                ? item.getStandardCost()
                : nullSafe(balance.getAverageCost());

        BigDecimal chargeAmount = billingPrice.multiply(quantity).setScale(4, RoundingMode.HALF_UP);

        PatientCharge charge = new PatientCharge();
        charge.setPatientAccount(patientAccount);
        charge.setDepartment(patientAccount.getDepartment());
        charge.setServiceCategory("PHARMACY");
        charge.setChargeDate(LocalDate.now());
        charge.setDueDate(LocalDate.now());
        charge.setAmount(chargeAmount);
        charge.setPaidAmount(BigDecimal.ZERO);
        charge.setStatus("POSTED");
        charge.setAccountingStatus("NOT_SUBMITTED");
        charge.setGlStatus("NOT_POSTED");

        patientChargeRepository.save(charge);

        issueStock(
                locationId,
                itemId,
                batchNumber,
                quantity,
                "PATIENT_CHARGE",
                charge.getId(),
                notes
        );

        glIntegrationService.postPatientChargeSafely(charge.getId());

        return charge;
    }

    public List<InventoryBalanceLine> balanceLines(UUID locationId) {
        List<InventoryBalance> balances = locationId != null
                ? balanceRepository.findByLocationId(locationId)
                : balanceRepository.findAll();

        return balances.stream()
                .filter(balance -> nullSafe(balance.getQuantityOnHand()).signum() != 0)
                .map(this::toBalanceLine)
                .sorted(Comparator.comparing(InventoryBalanceLine::getItemCode))
                .toList();
    }

    public List<InventoryBalanceLine> expiringBalances(int days) {
        LocalDate limit = LocalDate.now().plusDays(days);

        return balanceRepository.findAll()
                .stream()
                .filter(balance -> nullSafe(balance.getQuantityOnHand()).signum() > 0)
                .filter(balance -> balance.getBatch() != null)
                .filter(balance -> balance.getBatch().getExpiryDate() != null)
                .filter(balance -> !balance.getBatch().getExpiryDate().isAfter(limit))
                .map(this::toBalanceLine)
                .sorted(Comparator.comparing(InventoryBalanceLine::getExpiryDate))
                .toList();
    }

    public BigDecimal valuation(UUID locationId) {
        return balanceLines(locationId)
                .stream()
                .map(line -> nullSafe(line.getStockValue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private InventoryBalanceLine toBalanceLine(InventoryBalance balance) {
        BigDecimal quantity = nullSafe(balance.getQuantityOnHand());
        BigDecimal cost = nullSafe(balance.getAverageCost());

        return new InventoryBalanceLine(
                balance.getItem().getCode(),
                balance.getItem().getName(),
                balance.getLocation().getCode(),
                balance.getBatch() != null ? balance.getBatch().getBatchNumber() : null,
                balance.getBatch() != null ? balance.getBatch().getExpiryDate() : null,
                quantity,
                cost,
                quantity.multiply(cost).setScale(4, RoundingMode.HALF_UP)
        );
    }

    private SupplierBatch resolveBatch(
            InventoryLocation location,
            InventoryItem item,
            String batchNumber,
            LocalDate expiryDate,
            BigDecimal unitCost
    ) {
        if (batchNumber == null || batchNumber.isBlank()) {
            if (item.isBatchControlled()) {
                throw new IllegalArgumentException("Item is batch controlled. Batch number is required.");
            }

            return null;
        }

        if (item.isExpiryControlled() && expiryDate == null) {
            throw new IllegalArgumentException("Item is expiry controlled. Expiry date is required.");
        }

        SupplierBatch batch = batchRepository.findByBatchNumberAndItemId(batchNumber, item.getId())
                .orElse(null);

        if (batch != null) {
            return batch;
        }

        SupplierBatch newBatch = new SupplierBatch();
        newBatch.setBatchNumber(batchNumber);
        newBatch.setItem(item);
        newBatch.setDepartment(location.getDepartment());
        newBatch.setExpiryDate(expiryDate);
        newBatch.setQuantity(BigDecimal.ZERO);
        newBatch.setUnitCost(unitCost);
        newBatch.setAmount(BigDecimal.ZERO);
        newBatch.setStatus("ACTIVE");

        return batchRepository.save(newBatch);
    }

    private SupplierBatch findBatch(InventoryItem item, String batchNumber) {
        if (batchNumber == null || batchNumber.isBlank()) {
            if (item.isBatchControlled()) {
                throw new IllegalArgumentException("Item is batch controlled. Batch number is required.");
            }

            return null;
        }

        return batchRepository.findByBatchNumberAndItemId(batchNumber, item.getId())
                .orElseThrow(() -> new IllegalArgumentException("Batch not found: " + batchNumber));
    }

    private InventoryBalance findBalance(InventoryItem item, InventoryLocation location, SupplierBatch batch) {
        if (batch == null) {
            return balanceRepository.findByItemIdAndLocationIdAndBatchIsNull(item.getId(), location.getId())
                    .orElse(null);
        }

        return balanceRepository.findByItemIdAndLocationIdAndBatchId(item.getId(), location.getId(), batch.getId())
                .orElse(null);
    }

    private InventoryBalance findOrCreateBalance(InventoryItem item, InventoryLocation location, SupplierBatch batch) {
        InventoryBalance balance = findBalance(item, location, batch);

        if (balance != null) {
            return balance;
        }

        InventoryBalance newBalance = new InventoryBalance();
        newBalance.setItem(item);
        newBalance.setLocation(location);
        newBalance.setBatch(batch);
        newBalance.setQuantityOnHand(BigDecimal.ZERO);
        newBalance.setAverageCost(BigDecimal.ZERO);

        return newBalance;
    }

    private InventoryTransaction createTransaction(
            InventoryItem item,
            InventoryLocation location,
            SupplierBatch batch,
            String movementType,
            BigDecimal quantity,
            BigDecimal unitCost,
            String referenceType,
            UUID referenceId,
            String notes
    ) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setTransactionNumber("INV-" + UUID.randomUUID());
        transaction.setItem(item);
        transaction.setLocation(location);
        transaction.setBatch(batch);
        transaction.setMovementType(movementType);
        transaction.setQuantity(quantity);
        transaction.setUnitCost(nullSafe(unitCost));
        transaction.setAmount(nullSafe(quantity).multiply(nullSafe(unitCost)).setScale(4, RoundingMode.HALF_UP));
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setStatus("POSTED");
        transaction.setNotes(notes);
        transaction.setCreatedBy(currentUserService.username());

        return transactionRepository.save(transaction);
    }

    private InventoryLocation getLocation(UUID locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory location not found"));
    }

    private InventoryItem getItem(UUID itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory item not found"));
    }

    private void validatePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String appendNote(String existing, String additional) {
        if (existing == null || existing.isBlank()) {
            return additional;
        }

        return existing + System.lineSeparator() + additional;
    }
}