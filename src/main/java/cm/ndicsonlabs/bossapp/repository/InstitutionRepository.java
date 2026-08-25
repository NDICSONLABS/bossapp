package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.Institution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InstitutionRepository extends JpaRepository<Institution, UUID> {
}