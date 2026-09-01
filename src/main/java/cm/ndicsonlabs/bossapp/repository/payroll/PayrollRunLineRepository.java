// src/main/java/com/institution/finance/repository/PayrollRunLineRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.PayrollRunLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollRunLineRepository extends JpaRepository<PayrollRunLine, UUID> {

    List<PayrollRunLine> findByEmployeePayrollRunIdOrderByCreatedAtAsc(UUID employeePayrollRunId);

    void deleteByEmployeePayrollRunId(UUID employeePayrollRunId);
}