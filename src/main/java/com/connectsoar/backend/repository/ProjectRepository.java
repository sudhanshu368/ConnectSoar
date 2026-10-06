package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.ProjectStatus;
import com.connectsoar.backend.model.Project;
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
public class ProjectRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectRepository.class);

    private final Map<String, Project> projectStorage = new ConcurrentHashMap<>();
    private final RestClient supabasePostgrestRestClient;
    private final ObjectMapper objectMapper;
    private volatile long lastSyncTimestamp = 0;
    private static final long SYNC_INTERVAL_MS = 30_000;

    public ProjectRepository(
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
            log.info("Syncing projects from Supabase PostgREST...");
            String json = supabasePostgrestRestClient.get()
                    .uri("/projects?select=*")
                    .retrieve()
                    .body(String.class);

            if (json != null && !json.isBlank()) {
                JsonNode array = objectMapper.readTree(json);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        Project project = mapJsonToProject(node);
                        if (project != null && project.getId() != null) {
                            projectStorage.put(project.getId(), project);
                        }
                    }
                    lastSyncTimestamp = System.currentTimeMillis();
                    log.info("Successfully synced {} projects from Supabase", projectStorage.size());
                }
            }
        } catch (Throwable t) {
            log.warn("Supabase projects sync note: {}", t.getMessage());
        }
    }

    private void ensureFreshData() {
        if (projectStorage.isEmpty() || (System.currentTimeMillis() - lastSyncTimestamp > SYNC_INTERVAL_MS)) {
            syncFromSupabase();
        }
    }

    private Project mapJsonToProject(JsonNode node) {
        try {
            String id = node.path("id").asText(null);
            if (id == null || id.isBlank()) return null;

            String name = node.path("name").asText("");
            String description = node.path("description").asText(null);
            ProjectStatus status = ProjectStatus.fromString(node.path("status").asText("IN_PROGRESS"));
            
            LocalDate startDate = node.hasNonNull("start_date") ? LocalDate.parse(node.path("start_date").asText()) : LocalDate.now();
            LocalDate deadline = node.hasNonNull("deadline") ? LocalDate.parse(node.path("deadline").asText()) : LocalDate.now().plusMonths(1);
            String createdBy = node.path("created_by_user_id").asText(null);

            LocalDateTime lastActivityAt = parseDateTime(node.path("last_activity_at").asText(null));
            LocalDateTime createdAt = parseDateTime(node.path("created_at").asText(null));
            LocalDateTime updatedAt = parseDateTime(node.path("updated_at").asText(null));

            return Project.builder()
                    .id(id)
                    .name(name)
                    .description(description)
                    .status(status)
                    .startDate(startDate)
                    .deadline(deadline)
                    .createdByUserId(createdBy)
                    .lastActivityAt(lastActivityAt != null ? lastActivityAt : LocalDateTime.now())
                    .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                    .updatedAt(updatedAt != null ? updatedAt : LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.warn("Error parsing project JSON: {}", e.getMessage());
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

    public Project save(Project project) {
        if (project.getCreatedAt() == null) {
            project.setCreatedAt(LocalDateTime.now());
        }
        if (project.getLastActivityAt() == null) {
            project.setLastActivityAt(LocalDateTime.now());
        }
        project.setUpdatedAt(LocalDateTime.now());
        projectStorage.put(project.getId(), project);

        // Sync with Supabase
        try {
            if (supabasePostgrestRestClient != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", project.getId());
                map.put("name", project.getName());
                map.put("description", project.getDescription());
                map.put("status", project.getStatus().name());
                if (project.getStartDate() != null) map.put("start_date", project.getStartDate().toString());
                if (project.getDeadline() != null) map.put("deadline", project.getDeadline().toString());
                map.put("created_by_user_id", project.getCreatedByUserId());
                if (project.getLastActivityAt() != null) map.put("last_activity_at", project.getLastActivityAt().toString());

                supabasePostgrestRestClient.post()
                        .uri("/projects")
                        .header("Prefer", "resolution=merge-duplicates")
                        .body(map)
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Throwable t) {
            log.warn("Failed to persist project {} to Supabase: {}", project.getId(), t.getMessage());
        }

        return project;
    }

    public Optional<Project> findById(String id) {
        if (id == null) return Optional.empty();
        Project p = projectStorage.get(id);
        if (p == null) {
            ensureFreshData();
            p = projectStorage.get(id);
        }
        return Optional.ofNullable(p);
    }

    public List<Project> findAll() {
        ensureFreshData();
        return projectStorage.values().stream()
                .sorted(Comparator.comparing(Project::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    public List<Project> findActiveProjectsInactiveSince(LocalDateTime cutoff) {
        ensureFreshData();
        return projectStorage.values().stream()
                .filter(p -> p.getStatus() == ProjectStatus.IN_PROGRESS)
                .filter(p -> p.getLastActivityAt() != null && p.getLastActivityAt().isBefore(cutoff))
                .collect(Collectors.toList());
    }

    public void deleteById(String id) {
        if (id != null) {
            projectStorage.remove(id);
            try {
                if (supabasePostgrestRestClient != null) {
                    supabasePostgrestRestClient.delete()
                            .uri("/projects?id=eq." + id)
                            .retrieve()
                            .toBodilessEntity();
                }
            } catch (Throwable t) {
                log.warn("Failed to delete project {} from Supabase: {}", id, t.getMessage());
            }
        }
    }
}
