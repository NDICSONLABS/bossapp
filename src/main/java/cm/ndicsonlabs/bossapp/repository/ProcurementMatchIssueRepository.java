// src/main/java/com/institution/finance/repository/ProcurementMatchIssueRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.ProcurementMatchIssue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProcurementMatchIssueRepository extends JpaRepository<ProcurementMatchIssue, UUID> {

    List<ProcurementMatchIssue> findBySupplierInvoiceId(UUID supplierInvoiceId);

    void deleteBySupplierInvoiceId(UUID supplierInvoiceId);
}