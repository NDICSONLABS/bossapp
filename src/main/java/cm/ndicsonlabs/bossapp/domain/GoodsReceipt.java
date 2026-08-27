package cm.ndicsonlabs.bossapp.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "goods_receipt")
public class GoodsReceipt extends BaseEntity {

    @Column(name = "grn_number", nullable = false, unique = true)
    private String grnNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    @Column(name = "received_by")
    private String receivedBy;

    @Column(nullable = false)
    private String status = "RECEIVED";

    @Column(name = "delivery_note_number")
    private String deliveryNoteNumber;

    @Column(name = "delivery_note_date")
    private LocalDate deliveryNoteDate;

}