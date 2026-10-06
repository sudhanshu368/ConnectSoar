package com.connectsoar.backend.repository;

import com.connectsoar.backend.model.ProjectActivityLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProjectActivityLogRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectActivityLogRepository.class);

    private final Map<String, ProjectActivityLog> logStorage = new ConcurrentHashMap<>();
    private final RestClient supabasePostgrestRestClient;
    private final ObjectMapper objectMapper;

    public ProjectActivityLogRepository(
            @Autowired(required = false) @Qualifier("supabasePostgrestRestClient") RestClient supabasePostgrestRestClient,
            ObjectMapper objectMapper) {
        this.supabasePostgrestRestClient = supabasePostgrestRestClient;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        syncFromSupabase();
    }

    public synchronized void syncFromSupabase() {
        if (supabasePostgrestRestClient == null) return;
        try {
            log.info("Syncing project activity logs from Supabase...");
            String json = supabasePostgrestRestClient.get()
                    .uri("/project_activity_logs?select=*&order=created_at.desc&limit=200")
                    .retrieve()
                    .body(String.class);

            if (json != null && !json.isBlank()) {
                JsonNode array = objectMapper.readTree(json);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        ProjectActivityLog activityLog = mapJsonToLog(node);
                        if (activityLog != null && activityLog.getId() != null) {
                            logStorage.put(activityLog.getId(), activityLog);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            log.warn("Supabase project activity logs sync note: {}", t.getMessage());
        }
    }

    private ProjectActivityLog mapJsonToLog(JsonNode node) {
        try {
            String id = node.path("id").asText(null);
            if (id == null || id.isBlank()) return null;

            String projectId = node.path("project_id").asText(null);
            String moduleId = node.path("module_id").asText(null);
            String userId = node.path("user_id").asText(null);
            String userName = node.path("user_name").asText("");
            String action = node.path("action").asText("");
            String oldStatus = node.path("old_status").asText(null);
            String newStatus = node.path("new_status").asText(null);
            String details = node.path("details").asText(null);
            LocalDateTime createdAt = parseDateTime(node.path("created_at").asText(null));

            return ProjectActivityLog.builder()
                    .id(id)
                    .projectId(projectId)
                    .moduleId(moduleId)
                    .userId(userId)
                    .userName(userName)
                    .action(action)
                    .oldStatus(oldStatus)
                    .newStatus(newStatus)
                    .details(details)
                    .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return OffsetDateTime.parse(str).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(str);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    public ProjectActivityLog save(ProjectActivityLog activityLog) {
        if (activityLog.getCreatedAt() == null) {
            activityLog.setCreatedAt(LocalDateTime.now());
        }
        logStorage.put(activityLog.getId(), activityLog);

        try {
            if (supabasePostgrestRestClient != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", activityLog.getId());
                map.put("project_id", activityLog.getProjectId());
                map.put("module_id", activityLog.getModuleId());
                map.put("user_id", activityLog.getUserId());
                map.put("user_name", activityLog.getUserName());
                map.put("action", activityLog.getAction());
                map.put("old_status", activityLog.getOldStatus());
                map.put("new_status", activityLog.getNewStatus());
                map.put("details", activityLog.getDetails());

                supabasePostgrestRestClient.post()
                        .uri("/project_activity_logs")
                        .header("Prefer", "resolution=merge-duplicates")
                        .body(map)
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Throwable t) {
            log.warn("Failed to persist activity log to Supabase: {}", t.getMessage());
        }

        return activityLog;
    }

    public List<ProjectActivityLog> findByProjectId(String projectId) {
        return logStorage.values().stream()
                .filter(l -> projectId != null && projectId.equals(l.getProjectId()))
                .sorted(Comparator.comparing(ProjectActivityLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public void deleteByProjectId(String projectId) {
        List<ProjectActivityLog> list = findByProjectId(projectId);
        for (ProjectActivityLog l : list) {
            logStorage.remove(l.getId());
        }
    }
}
