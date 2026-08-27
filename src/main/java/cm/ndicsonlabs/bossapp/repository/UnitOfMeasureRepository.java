// src/main/java/com/institution/finance/repository/UnitOfMeasureRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.UnitOfMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasure, UUID> {
}