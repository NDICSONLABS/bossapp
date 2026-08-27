// src/main/java/com/institution/finance/domain/GoodsReceiptLine.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "goods_receipt_line")
public class GoodsReceiptLine extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_line_id", nullable = false)
    private PurchaseOrderLine purchaseOrderLine;

    @Column(name = "quantity_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityOrdered = BigDecimal.ZERO;

    @Column(name = "quantity_received", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReceived = BigDecimal.ZERO;

    @Column(name = "accepted_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal acceptedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private InventoryItem item;
}