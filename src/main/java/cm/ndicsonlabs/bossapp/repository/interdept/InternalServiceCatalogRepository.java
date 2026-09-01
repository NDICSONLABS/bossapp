// src/main/java/com/institution/finance/repository/InternalServiceCatalogRepository.java
package cm.ndicsonlabs.bossapp.repository.interdept;

import cm.ndicsonlabs.bossapp.domain.interdept.InternalServiceCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InternalServiceCatalogRepository extends JpaRepository<InternalServiceCatalog, UUID> {

    List<InternalServiceCatalog> findByActiveTrueOrderByCode();
}