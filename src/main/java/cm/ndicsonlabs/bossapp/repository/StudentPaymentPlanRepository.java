// src/main/java/com/institution/finance/repository/StudentPaymentPlanRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.StudentPaymentPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentPaymentPlanRepository extends JpaRepository<StudentPaymentPlan, UUID> {
}