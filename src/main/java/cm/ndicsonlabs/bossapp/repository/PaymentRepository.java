// src/main/java/com/institution/finance/repository/PaymentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Department;
import cm.ndicsonlabs.bossapp.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByDepartmentAndPaymentDateBetweenAndAccountingStatusIn(
            Department department,
            LocalDate start,
            LocalDate end,
            Collection<String> statuses
    );
}