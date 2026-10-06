package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.ProjectMemberRole;
import com.connectsoar.backend.model.ProjectMember;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProjectMemberRepository {

    private static final Logger log = LoggerFactory.getLogger(ProjectMemberRepository.class);

    private final Map<String, ProjectMember> memberStorage = new ConcurrentHashMap<>();
    private final RestClient supabasePostgrestRestClient;
    private final ObjectMapper objectMapper;
    private volatile long lastSyncTimestamp = 0;
    private static final long SYNC_INTERVAL_MS = 30_000;

    public ProjectMemberRepository(
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
            log.info("Syncing project members from Supabase...");
            String json = supabasePostgrestRestClient.get()
                    .uri("/project_members?select=*")
                    .retrieve()
                    .body(String.class);

            if (json != null && !json.isBlank()) {
                JsonNode array = objectMapper.readTree(json);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        ProjectMember member = mapJsonToMember(node);
                        if (member != null && member.getId() != null) {
                            memberStorage.put(member.getId(), member);
                        }
                    }
                    lastSyncTimestamp = System.currentTimeMillis();
                }
            }
        } catch (Throwable t) {
            log.warn("Supabase project members sync note: {}", t.getMessage());
        }
    }

    private void ensureFreshData() {
        if (memberStorage.isEmpty() || (System.currentTimeMillis() - lastSyncTimestamp > SYNC_INTERVAL_MS)) {
            syncFromSupabase();
        }
    }

    private ProjectMember mapJsonToMember(JsonNode node) {
        try {
            String id = node.path("id").asText(null);
            if (id == null || id.isBlank()) return null;

            String projectId = node.path("project_id").asText(null);
            String userId = node.path("user_id").asText(null);
            ProjectMemberRole role = ProjectMemberRole.fromString(node.path("member_role").asText("FULL_STACK"));
            LocalDateTime assignedAt = parseDateTime(node.path("assigned_at").asText(null));

            return ProjectMember.builder()
                    .id(id)
                    .projectId(projectId)
                    .userId(userId)
                    .memberRole(role)
                    .assignedAt(assignedAt != null ? assignedAt : LocalDateTime.now())
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

    public ProjectMember save(ProjectMember member) {
        if (member.getAssignedAt() == null) {
            member.setAssignedAt(LocalDateTime.now());
        }
        memberStorage.put(member.getId(), member);

        try {
            if (supabasePostgrestRestClient != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", member.getId());
                map.put("project_id", member.getProjectId());
                map.put("user_id", member.getUserId());
                map.put("member_role", member.getMemberRole().name());

                supabasePostgrestRestClient.post()
                        .uri("/project_members")
                        .header("Prefer", "resolution=merge-duplicates")
                        .body(map)
                        .retrieve()
                        .toBodilessEntity();
            }
        } catch (Throwable t) {
            log.warn("Failed to persist project member {} to Supabase: {}", member.getId(), t.getMessage());
        }

        return member;
    }

    public List<ProjectMember> findByProjectId(String projectId) {
        ensureFreshData();
        return memberStorage.values().stream()
                .filter(m -> projectId != null && projectId.equals(m.getProjectId()))
                .collect(Collectors.toList());
    }

    public List<ProjectMember> findByUserId(String userId) {
        ensureFreshData();
        return memberStorage.values().stream()
                .filter(m -> userId != null && userId.equals(m.getUserId()))
                .collect(Collectors.toList());
    }

    public Optional<ProjectMember> findByProjectIdAndUserId(String projectId, String userId) {
        ensureFreshData();
        return memberStorage.values().stream()
                .filter(m -> projectId != null && projectId.equals(m.getProjectId()) &&
                             userId != null && userId.equals(m.getUserId()))
                .findFirst();
    }

    public void deleteByProjectIdAndUserId(String projectId, String userId) {
        findByProjectIdAndUserId(projectId, userId).ifPresent(m -> {
            memberStorage.remove(m.getId());
            try {
                if (supabasePostgrestRestClient != null) {
                    supabasePostgrestRestClient.delete()
                            .uri("/project_members?id=eq." + m.getId())
                            .retrieve()
                            .toBodilessEntity();
                }
            } catch (Throwable ignored) {}
        });
    }

    public void deleteByProjectId(String projectId) {
        List<ProjectMember> toRemove = findByProjectId(projectId);
        for (ProjectMember m : toRemove) {
            memberStorage.remove(m.getId());
        }
    }
}
