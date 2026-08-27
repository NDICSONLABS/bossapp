// src/main/java/com/institution/finance/domain/GlIntegrationLog.java
package cm.ndicsonlabs.bossapp.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "gl_integration_log")
public class GlIntegrationLog extends BaseEntity {

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String message;
}