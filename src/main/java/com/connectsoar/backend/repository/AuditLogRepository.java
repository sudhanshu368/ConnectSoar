package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.AuditLog;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class AuditLogRepository {

    private final Map<String, AuditLog> logStorage = new ConcurrentHashMap<>();

    public AuditLog save(AuditLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        logStorage.put(log.getId(), log);
        return log;
    }

    public List<AuditLog> findAll() {
        return logStorage.values().stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<AuditLog> findAllByActorUserId(String actorUserId) {
        return logStorage.values().stream()
                .filter(l -> actorUserId.equals(l.getActorUserId()))
                .sorted(Comparator.comparing(AuditLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public void clear() {
        logStorage.clear();
    }
}
