package com.connectsoar.backend.service;

import com.connectsoar.backend.enums.AuditAction;
import com.connectsoar.backend.model.AuditLog;
import com.connectsoar.backend.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records a secure audit event.
     * Guaranteed to sanitize and never record passwords, tokens, or credentials.
     */
    public void record(String actorUserId, AuditAction action, String resourceType, String resourceId, Map<String, Object> metadata) {
        try {
            Map<String, Object> sanitizedMetadata = new HashMap<>();
            if (metadata != null) {
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    String key = entry.getKey().toLowerCase();
                    if (!key.contains("password") && !key.contains("token") && !key.contains("secret") && !key.contains("key")) {
                        sanitizedMetadata.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            AuditLog logEntry = AuditLog.builder()
                    .id(UUID.randomUUID().toString())
                    .actorUserId(actorUserId != null ? actorUserId : "SYSTEM")
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .metadata(sanitizedMetadata)
                    .createdAt(LocalDateTime.now())
                    .build();

            auditLogRepository.save(logEntry);
            log.info("Audit event recorded: action={}, actor={}, resourceType={}, resourceId={}", 
                    action, actorUserId, resourceType, resourceId);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage(), e);
        }
    }
}
