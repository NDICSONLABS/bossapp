// src/main/java/com/institution/finance/service/AuditService.java
package cm.ndicsonlabs.bossapp.service;

import cm.ndicsonlabs.bossapp.domain.AuditLog;
import cm.ndicsonlabs.bossapp.repository.AuditLogRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(
            String entityType,
            UUID entityId,
            String action,
            String beforeValue,
            String afterValue,
            String reason
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUsername(currentUsername());
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId != null ? entityId.toString() : null);
        auditLog.setAction(action);
        auditLog.setBeforeValue(beforeValue);
        auditLog.setAfterValue(afterValue);
        auditLog.setReason(reason);

        auditLogRepository.save(auditLog);
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return "system";
        }

        return authentication.getName();
    }
}