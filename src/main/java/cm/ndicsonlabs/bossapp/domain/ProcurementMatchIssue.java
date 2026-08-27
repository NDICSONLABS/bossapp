// src/main/java/com/institution/finance/domain/ProcurementMatchIssue.java
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
@Table(name = "procurement_match_issue")
public class ProcurementMatchIssue extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_invoice_id", nullable = false)
    private SupplierInvoice supplierInvoice;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id")
    private PurchaseOrder purchaseOrder;

    @ManyToOne
    @JoinColumn(name = "goods_receipt_id")
    private GoodsReceipt goodsReceipt;

    @Column(name = "issue_type", nullable = false)
    private String issueType;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(precision = 19, scale = 4)
    private BigDecimal amount;
}