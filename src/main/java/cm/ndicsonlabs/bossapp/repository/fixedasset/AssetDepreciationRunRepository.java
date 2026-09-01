// src/main/java/com/institution/finance/repository/AssetDepreciationRunRepository.java
package cm.ndicsonlabs.bossapp.repository.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetDepreciationRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AssetDepreciationRunRepository extends JpaRepository<AssetDepreciationRun, UUID> {
    Optional<AssetDepreciationRun> findByPeriodYearAndPeriodMonth(Integer year, Integer month);
}