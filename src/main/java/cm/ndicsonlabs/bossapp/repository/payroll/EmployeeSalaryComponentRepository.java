// src/main/java/com/institution/finance/repository/EmployeeSalaryComponentRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.EmployeeSalaryComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeSalaryComponentRepository extends JpaRepository<EmployeeSalaryComponent, UUID> {

    List<EmployeeSalaryComponent> findByEmployeeIdAndActiveTrue(UUID employeeId);
}