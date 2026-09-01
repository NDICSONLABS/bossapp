// src/main/java/com/institution/finance/repository/AssetCategoryRepository.java
package cm.ndicsonlabs.bossapp.repository.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, UUID> {
    List<AssetCategory> findByActiveTrueOrderByCode();
}