// src/main/java/com/institution/finance/repository/StudentChargeAdjustmentRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.StudentChargeAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentChargeAdjustmentRepository extends JpaRepository<StudentChargeAdjustment, UUID> {
}