package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;
import com.connectsoar.backend.model.Profile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProfileRepository {

    private static final Logger log = LoggerFactory.getLogger(ProfileRepository.class);

    private final Map<String, Profile> profileStorage = new ConcurrentHashMap<>();
    private final RestClient supabasePostgrestRestClient;
    private final ObjectMapper objectMapper;
    private volatile long lastSyncTimestamp = 0;
    private static final long SYNC_INTERVAL_MS = 30_000;

    public ProfileRepository(
            @Qualifier("supabasePostgrestRestClient") RestClient supabasePostgrestRestClient,
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
            log.info("Syncing profiles from Supabase PostgREST...");
            String json = supabasePostgrestRestClient.get()
                    .uri("/profiles?select=*")
                    .retrieve()
                    .body(String.class);

            if (json != null && !json.isBlank()) {
                JsonNode array = objectMapper.readTree(json);
                if (array.isArray()) {
                    for (JsonNode node : array) {
                        Profile profile = mapJsonToProfile(node);
                        if (profile != null && profile.getId() != null) {
                            profileStorage.put(profile.getId(), profile);
                        }
                    }
                    lastSyncTimestamp = System.currentTimeMillis();
                    log.info("Successfully synced {} profiles from Supabase", profileStorage.size());
                }
            }
        } catch (Throwable t) {
            log.warn("Supabase profiles sync note: {}", t.getMessage());
        }
    }

    private void ensureFreshData() {
        if (profileStorage.isEmpty() || (System.currentTimeMillis() - lastSyncTimestamp > SYNC_INTERVAL_MS)) {
            syncFromSupabase();
        }
    }

    private Profile mapJsonToProfile(JsonNode node) {
        try {
            String id = node.path("id").asText(null);
            if (id == null || id.isBlank()) return null;

            String email = node.path("email").asText(null);
            String name = node.path("name").asText("");
            Role role = Role.fromString(node.path("role").asText("employee"));
            UserStatus status = UserStatus.fromString(node.path("status").asText("active"));
            String department = node.hasNonNull("department") ? node.path("department").asText() : null;
            String designation = node.hasNonNull("designation") ? node.path("designation").asText() : null;
            String phone = node.hasNonNull("phone") ? node.path("phone").asText() : null;
            String address = node.hasNonNull("address") ? node.path("address").asText() : null;
            String adharNumber = node.hasNonNull("adhar_number") ? node.path("adhar_number").asText() : null;
            String imageUrl = node.hasNonNull("image_url") ? node.path("image_url").asText() : null;
            boolean resetPassword = node.path("reset_password").asBoolean(false);

            LocalDateTime createdAt = parseDateTime(node.path("created_at").asText(null));
            LocalDateTime updatedAt = parseDateTime(node.path("updated_at").asText(null));

            return Profile.builder()
                    .id(id)
                    .email(email)
                    .name(name)
                    .role(role)
                    .status(status)
                    .department(department)
                    .designation(designation)
                    .phone(phone)
                    .address(address)
                    .adharNumber(adharNumber)
                    .imageUrl(imageUrl)
                    .resetPassword(resetPassword)
                    .createdAt(createdAt != null ? createdAt : LocalDateTime.now())
                    .updatedAt(updatedAt != null ? updatedAt : LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.warn("Error parsing profile JSON: {}", e.getMessage());
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

    public Profile save(Profile profile) {
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        profileStorage.put(profile.getId(), profile);

        // Sync with Supabase PostgREST
        try {
            if (supabasePostgrestRestClient != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", profile.getId());
                map.put("email", profile.getEmail());
                map.put("name", profile.getName());
                map.put("role", profile.getRole() != null ? profile.getRole().name() : "employee");
                map.put("status", profile.getStatus() != null ? profile.getStatus().name() : "active");
                map.put("department", profile.getDepartment());
                map.put("designation", profile.getDesignation());
                map.put("phone", profile.getPhone());
                map.put("address", profile.getAddress());
                map.put("adhar_number", profile.getAdharNumber());
                map.put("image_url", profile.getImageUrl());
                map.put("reset_password", profile.isResetPassword());

                supabasePostgrestRestClient.post()
                        .uri("/profiles")
                        .header("Prefer", "resolution=merge-duplicates")
                        .body(map)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Persisted profile to Supabase: {}", profile.getEmail());
            }
        } catch (Throwable t) {
            log.warn("Failed to persist profile {} to Supabase: {}", profile.getEmail(), t.getMessage());
        }

        return profile;
    }

    public Optional<Profile> findById(String id) {
        if (id == null) return Optional.empty();
        Profile p = profileStorage.get(id);
        if (p == null) {
            ensureFreshData();
            p = profileStorage.get(id);
        }
        return Optional.ofNullable(p);
    }

    public Optional<Profile> findByEmail(String email) {
        if (email == null) return Optional.empty();
        Optional<Profile> found = profileStorage.values().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst();
        if (found.isEmpty()) {
            ensureFreshData();
            found = profileStorage.values().stream()
                    .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                    .findFirst();
        }
        return found;
    }

    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    public List<Profile> findAll() {
        ensureFreshData();
        return new ArrayList<>(profileStorage.values());
    }

    public List<Profile> findEmployees(String search, UserStatus status, String department, int page, int limit) {
        return findEmployees(search, status, department, null, page, limit);
    }

    public List<Profile> findEmployees(String search, UserStatus status, String department, Role role, int page, int limit) {
        ensureFreshData();
        final String searchLower = (search != null) ? search.trim().toLowerCase() : null;
        return profileStorage.values().stream()
                .filter(p -> role == null || p.getRole() == role)
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> department == null || department.isBlank() || (p.getDepartment() != null && p.getDepartment().equalsIgnoreCase(department.trim())))
                .filter(p -> searchLower == null || searchLower.isEmpty() ||
                        (p.getName() != null && p.getName().toLowerCase().contains(searchLower)) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(searchLower)) ||
                        (p.getPhone() != null && p.getPhone().toLowerCase().contains(searchLower)) ||
                        (p.getAddress() != null && p.getAddress().toLowerCase().contains(searchLower)) ||
                        (p.getAdharNumber() != null && p.getAdharNumber().toLowerCase().contains(searchLower)) ||
                        (p.getDepartment() != null && p.getDepartment().toLowerCase().contains(searchLower)) ||
                        (p.getDesignation() != null && p.getDesignation().toLowerCase().contains(searchLower)))
                .sorted(Comparator.comparing(Profile::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long countEmployees(String search, UserStatus status, String department) {
        return countEmployees(search, status, department, null);
    }

    public long countEmployees(String search, UserStatus status, String department, Role role) {
        ensureFreshData();
        final String searchLower = (search != null) ? search.trim().toLowerCase() : null;
        return profileStorage.values().stream()
                .filter(p -> role == null || p.getRole() == role)
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> department == null || department.isBlank() || (p.getDepartment() != null && p.getDepartment().equalsIgnoreCase(department.trim())))
                .filter(p -> searchLower == null || searchLower.isEmpty() ||
                        (p.getName() != null && p.getName().toLowerCase().contains(searchLower)) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(searchLower)) ||
                        (p.getPhone() != null && p.getPhone().toLowerCase().contains(searchLower)) ||
                        (p.getAddress() != null && p.getAddress().toLowerCase().contains(searchLower)) ||
                        (p.getAdharNumber() != null && p.getAdharNumber().toLowerCase().contains(searchLower)) ||
                        (p.getDepartment() != null && p.getDepartment().toLowerCase().contains(searchLower)) ||
                        (p.getDesignation() != null && p.getDesignation().toLowerCase().contains(searchLower)))
                .count();
    }

    public void deleteById(String id) {
        if (id != null) {
            profileStorage.remove(id);
            try {
                if (supabasePostgrestRestClient != null) {
                    supabasePostgrestRestClient.delete()
                            .uri("/profiles?id=eq." + id)
                            .retrieve()
                            .toBodilessEntity();
                    log.info("Deleted profile from Supabase: {}", id);
                }
            } catch (Throwable t) {
                log.warn("Failed to delete profile {} from Supabase: {}", id, t.getMessage());
            }
        }
    }

    public void clear() {
        profileStorage.clear();
    }
}
