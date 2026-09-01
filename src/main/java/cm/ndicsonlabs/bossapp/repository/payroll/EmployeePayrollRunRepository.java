// src/main/java/com/institution/finance/repository/EmployeePayrollRunRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.EmployeePayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeePayrollRunRepository extends JpaRepository<EmployeePayrollRun, UUID> {

    List<EmployeePayrollRun> findByPayrollPeriodId(UUID payrollPeriodId);
}