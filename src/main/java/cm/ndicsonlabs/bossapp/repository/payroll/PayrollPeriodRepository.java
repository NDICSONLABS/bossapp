// src/main/java/com/institution/finance/repository/PayrollPeriodRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.PayrollPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, UUID> {

    List<PayrollPeriod> findByOrderByCreatedAtDesc();
}