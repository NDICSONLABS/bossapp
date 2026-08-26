// src/main/java/com/institution/finance/repository/StudentReceiptRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.StudentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentReceiptRepository extends JpaRepository<StudentReceipt, UUID> {
}