// src/main/java/com/institution/finance/repository/InternalInvoiceRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.InternalInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface InternalInvoiceRepository extends JpaRepository<InternalInvoice, UUID> {

    List<InternalInvoice> findByStatusOrderByCreatedAtDesc(String status);

    List<InternalInvoice> findByOrderByCreatedAtDesc();

    List<InternalInvoice> findByTransactionDateBetweenAndStatusIn(
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
}