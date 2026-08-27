// src/main/java/com/institution/finance/repository/GoodsReceiptLineRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GoodsReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, UUID> {

    List<GoodsReceiptLine> findByGoodsReceiptId(UUID goodsReceiptId);
}