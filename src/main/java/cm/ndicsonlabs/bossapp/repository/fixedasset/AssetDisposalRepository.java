// src/main/java/com/institution/finance/repository/AssetDisposalRepository.java
package cm.ndicsonlabs.bossapp.repository.fixedasset;

import cm.ndicsonlabs.bossapp.domain.fixedasset.AssetDisposal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AssetDisposalRepository extends JpaRepository<AssetDisposal, UUID> {
}