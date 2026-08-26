// src/main/java/com/institution/finance/repository/PaymentPlanInstallmentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PaymentPlanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentPlanInstallmentRepository extends JpaRepository<PaymentPlanInstallment, UUID> {

    List<PaymentPlanInstallment> findByPaymentPlanIdOrderByInstallmentNumberAsc(UUID paymentPlanId);
}