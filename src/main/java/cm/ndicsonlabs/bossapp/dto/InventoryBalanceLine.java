// src/main/java/com/institution/finance/report/InventoryBalanceLine.java
package cm.ndicsonlabs.bossapp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class InventoryBalanceLine {

    private String itemCode;
    private String itemName;
    private String locationCode;
    private String batchNumber;
    private LocalDate expiryDate;
    private BigDecimal quantityOnHand;
    private BigDecimal averageCost;
    private BigDecimal stockValue;

    public InventoryBalanceLine(
            String itemCode,
            String itemName,
            String locationCode,
            String batchNumber,
            LocalDate expiryDate,
            BigDecimal quantityOnHand,
            BigDecimal averageCost,
            BigDecimal stockValue
    ) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.locationCode = locationCode;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantityOnHand = quantityOnHand;
        this.averageCost = averageCost;
        this.stockValue = stockValue;
    }

    public String getItemCode() {
        return itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public BigDecimal getQuantityOnHand() {
        return quantityOnHand;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public BigDecimal getStockValue() {
        return stockValue;
    }
}