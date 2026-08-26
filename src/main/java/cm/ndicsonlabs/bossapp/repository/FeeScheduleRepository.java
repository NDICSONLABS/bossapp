// src/main/java/com/institution/finance/repository/FeeScheduleRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.FeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeeScheduleRepository extends JpaRepository<FeeSchedule, UUID> {
}