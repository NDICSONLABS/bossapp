// src/main/java/com/institution/finance/repository/AssetDepreciationLineRepository.java
package cm.ndicsonlabs.bossapp.repository.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetDepreciationLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AssetDepreciationLineRepository extends JpaRepository<AssetDepreciationLine, UUID> {
}