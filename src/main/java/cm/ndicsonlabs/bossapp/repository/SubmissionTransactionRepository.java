// src/main/java/com/institution/finance/repository/SubmissionTransactionRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.SubmissionTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubmissionTransactionRepository extends JpaRepository<SubmissionTransaction, UUID> {

    List<SubmissionTransaction> findBySubmissionId(UUID submissionId);
}