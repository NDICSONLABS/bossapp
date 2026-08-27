// src/main/java/com/institution/finance/repository/SupplierInvoiceRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.SupplierInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SupplierInvoiceRepository extends JpaRepository<SupplierInvoice, UUID> {
    List<SupplierInvoice> findByDepartmentAndInvoiceDateBetweenAndAccountingStatusIn(
            Department department,
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
    List<SupplierInvoice> findByGlStatusIn(Collection<String> statuses);

    long countByInvoiceDateBetweenAndGlStatusIn(
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
}