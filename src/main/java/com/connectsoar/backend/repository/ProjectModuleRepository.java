package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.ModuleStatus;
import com.connectsoar.backend.enums.ModuleType;
import com.connectsoar.backend.model.ProjectModule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProjectModuleRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectModuleRepository.class);

    private final Map<String, ProjectModule> moduleStorage = new ConcurrentHashMap<>();
    private final RestClient supabasePostgrestRestClient;
    private final ObjectMapper objectMapper;
    private volatile long lastSyncTimestamp = 0;
    private static final long SYNC_INTERVAL_MS = 30_000;

    public ProjectModuleRepository(
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
            log.info("Syncing project modules from Supabase...");
            String json = supabasePostgrestRestClient.get()
                    .uri("/project_modules?select=*")
                    .retrieve()
                    .body(String.class);

            if (json != null && !json.isBlank()) {
                JsonNode array = objectMapper.readTree(json);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        ProjectModule module = mapJsonToModule(node);
                        if (module != null && module.getId() != null) {
                            moduleStorage.put(module.getId(), module);
                        }
                    }
                    lastSyncTimestamp = System.currentTimeMillis();
                }
            }
        } catch (Throwable t) {
            log.warn("Supabase project modules sync note: {}", t.getMessage());
        }
    }

    private void ensureFreshData() {
        if (moduleStorage.isEmpty() || (System.currentTimeMillis() - lastSyncTimestamp > SYNC_INTERVAL_MS)) {
            syncFromSupabase();
        }
    }

    private ProjectModule mapJsonToModule(JsonNode node) {
        try {
            String id = node.path("id").asText(null);
            if (id == null || id.isBlank()) return null;

            String projectId = node.path("project_id").asText(null);
            String title = node.path("title").asText("");
            String description = node.path("description").asText(null);
            ModuleType moduleType = ModuleType.fromString(node.path("module_type").asText("FULL_STACK"));
            ModuleStatus status = ModuleStatus.fromString(node.path("status").asText("TODO"));
            int progress = node.path("progress_percentage").asInt(0);
            String assignedToUserId = node.path("assigned_to_user_id").asText(null);
            String assignedToName = node.path("assigned_to_name").asText(null);
            String createdByUserId = node.path("created_by_user_id").asText(null);
            LocalDate deadline = node.hasNonNull("deadline") ? LocalDate.parse(node.path("deadline").asText()) : null;
            String lastUpdatedByUserId = node.path("last_updated_by_user_id").asText(null);
            String lastUpdatedByName = node.path("last_updated_by_name").asText(null);
            LocalDateTime lastUpdatedAt = parseDateTime(node.path("last_updated_at").asText(null));
            LocalDateTime createdAt = parseDateTime(node.path("created_at").asText(null));
            LocalDateTime updatedAt = parseDateTime(node.path("updated_at").asText(null));

            return ProjectModule.builder()
                    .id(id)
                    .projectId(projectId)
                    .title(title)
                    .description(description)
                    .moduleType(moduleType)
                    .status(status)
                    .progressPercentage(progress)
                    .assignedToUserId(assignedToUserId)
                    .assignedToName(assignedToName)
                    .createdByUserId(createdByUserId)
                    .deadline(deadline)
                    .lastUpdatedByUserId(lastUpdatedByUserId)
                    .lastUpdatedByName(lastUpdatedByName)
                    .lastUpdatedAt(lastUpdatedAt != null ? lastUpdatedAt : LocalDateTime.now())
                    .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                    .updatedAt(updatedAt != null ? updatedAt : LocalDateTime.now())
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

    public ProjectModule save(ProjectModule module) {
        if (module.getCreatedAt() == null) {
            module.setCreatedAt(LocalDateTime.now());
        }
        if (module.getLastUpdatedAt() == null) {
            module.setLastUpdatedAt(LocalDateTime.now());
        }
        module.setUpdatedAt(LocalDateTime.now());
        moduleStorage.put(module.getId(), module);

        try {
            if (supabasePostgrestRestClient != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", module.getId());
                map.put("project_id", module.getProjectId());
                map.put("title", module.getTitle());
                map.put("description", module.getDescription());
                map.put("module_type", module.getModuleType().name());
                map.put("status", module.getStatus().name());
                map.put("progress_percentage", module.getProgressPercentage());
                map.put("assigned_to_user_id", module.getAssignedToUserId());
                map.put("assigned_to_name", module.getAssignedToName());
                map.put("created_by_user_id", module.getCreatedByUserId());
                if (module.getDeadline() != null) map.put("deadline", module.getDeadline().toString());
                map.put("last_updated_by_user_id", module.getLastUpdatedByUserId());
                map.put("last_updated_by_name", module.getLastUpdatedByName());
                if (module.getLastUpdatedAt() != null) map.put("last_updated_at", module.getLastUpdatedAt().toString());

                supabasePostgrestRestClient.post()
                        .uri("/project_modules")
                        .header("Prefer", "resolution=merge-duplicates")
                        .body(map)
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Throwable t) {
            log.warn("Failed to persist project module {} to Supabase: {}", module.getId(), t.getMessage());
        }

        return module;
    }

    public Optional<ProjectModule> findById(String id) {
        if (id == null) return Optional.empty();
        ProjectModule m = moduleStorage.get(id);
        if (m == null) {
            ensureFreshData();
            m = moduleStorage.get(id);
        }
        return Optional.ofNullable(m);
    }

    public List<ProjectModule> findByProjectId(String projectId) {
        ensureFreshData();
        return moduleStorage.values().stream()
                .filter(m -> projectId != null && projectId.equals(m.getProjectId()))
                .sorted(Comparator.comparing(ProjectModule::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public List<ProjectModule> findByAssignedToUserId(String userId) {
        ensureFreshData();
        return moduleStorage.values().stream()
                .filter(m -> userId != null && userId.equals(m.getAssignedToUserId()))
                .sorted(Comparator.comparing(ProjectModule::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        if (id != null) {
            moduleStorage.remove(id);
            try {
                if (supabasePostgrestRestClient != null) {
                    supabasePostgrestRestClient.delete()
                            .uri("/project_modules?id=eq." + id)
                            .retrieve()
                            .toBodilessEntity();
                }
            } catch (Throwable ignored) {}
        }
    }

    public void deleteByProjectId(String projectId) {
        List<ProjectModule> list = findByProjectId(projectId);
        for (ProjectModule m : list) {
            moduleStorage.remove(m.getId());
        }
    }
}
