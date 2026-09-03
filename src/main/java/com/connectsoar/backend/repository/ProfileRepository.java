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
        return profileStorage.values().stream()
                .filter(p -> p.getRole() == Role.employee)
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> department == null || department.isBlank() || (p.getDepartment() != null && p.getDepartment().equalsIgnoreCase(department)))
                .filter(p -> search == null || search.isBlank() || 
                        (p.getName() != null && p.getName().toLowerCase().contains(search.toLowerCase())) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(search.toLowerCase())))
                .sorted(Comparator.comparing(Profile::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .skip((long) (page - 1) * limit)
                .limit(limit)
                .collect(Collectors.toList());
    }

    public long countEmployees(String search, UserStatus status, String department) {
        return profileStorage.values().stream()
                .filter(p -> p.getRole() == Role.employee)
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> department == null || department.isBlank() || (p.getDepartment() != null && p.getDepartment().equalsIgnoreCase(department)))
                .filter(p -> search == null || search.isBlank() || 
                        (p.getName() != null && p.getName().toLowerCase().contains(search.toLowerCase())) ||
                        (p.getEmail() != null && p.getEmail().toLowerCase().contains(search.toLowerCase())))
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
