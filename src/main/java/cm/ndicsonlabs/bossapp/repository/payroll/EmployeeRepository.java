// src/main/java/com/institution/finance/repository/EmployeeRepository.java
package cm.ndicsonlabs.bossapp.repository.payroll;

import cm.ndicsonlabs.bossapp.domain.payroll.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    List<Employee> findByActiveTrueOrderByEmployeeNumber();
}