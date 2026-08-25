// src/main/java/com/institution/finance/repository/PatientAccountRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PatientAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientAccountRepository extends JpaRepository<PatientAccount, UUID> {
}