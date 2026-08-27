// src/main/java/com/institution/finance/repository/GlSettingRepository.java
package cm.ndicsonlabs.bossapp.repository;

import cm.ndicsonlabs.bossapp.domain.GlSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GlSettingRepository extends JpaRepository<GlSetting, String> {
}