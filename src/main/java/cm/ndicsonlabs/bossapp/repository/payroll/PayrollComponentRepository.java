// src/main/java/com/institution/finance/repository/PayrollComponentRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.PayrollComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollComponentRepository extends JpaRepository<PayrollComponent, UUID> {

    Optional<PayrollComponent> findByCode(String code);

    List<PayrollComponent> findByActiveTrueOrderByCode();
}