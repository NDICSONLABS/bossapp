// src/main/java/com/institution/finance/repository/PaymentAllocationRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.PaymentAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentAllocationRepository extends JpaRepository<PaymentAllocation, UUID> {
}