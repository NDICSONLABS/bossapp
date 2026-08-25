package cm.ndicsonlabs.bossapp.repository;


import cm.ndicsonlabs.bossapp.domain.InsuranceClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InsuranceClaimRepository extends JpaRepository<InsuranceClaim, UUID> {
}