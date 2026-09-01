// src/main/java/com/institution/finance/repository/FixedAssetRepository.java
package cm.ndicsonlabs.bossapp.repository.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FixedAssetRepository extends JpaRepository<FixedAsset, UUID> {
    List<FixedAsset> findByStatusIn(List<String> statuses);
    List<FixedAsset> findByDepartmentId(UUID departmentId);
}