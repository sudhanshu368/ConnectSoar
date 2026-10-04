package com.connectsoar.backend.repository;

import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;
import com.connectsoar.backend.model.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ProfileRepository {

    private final Map<String, Profile> profileStorage = new ConcurrentHashMap<>();

    public Profile save(Profile profile) {
        if (profile.getCreatedAt() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        profileStorage.put(profile.getId(), profile);
        return profile;
    }

    public Optional<Profile> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(profileStorage.get(id));
    }

    public Optional<Profile> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return profileStorage.values().stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmail()))
                .findFirst();
    }

    public boolean existsById(String id) {
        if (id == null) return false;
        return profileStorage.containsKey(id);
    }

    public boolean existsByEmail(String email) {
        if (email == null) return false;
        return profileStorage.values().stream()
                .anyMatch(p -> email.equalsIgnoreCase(p.getEmail()));
    }

    public List<Profile> findAll() {
        return new ArrayList<>(profileStorage.values());
    }

    public List<Profile> findEmployees(String search, UserStatus status, String department, int page, int limit) {
        return findEmployees(search, status, department, null, page, limit);
    }

    public List<Profile> findEmployees(String search, UserStatus status, String department, Role role, int page, int limit) {
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
        }
    }

    public void clear() {
        profileStorage.clear();
    }
}
