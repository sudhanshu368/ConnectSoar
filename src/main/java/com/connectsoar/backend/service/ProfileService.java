package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.PagedResponse;
import com.connectsoar.backend.dto.UpdateEmployeeRequest;
import com.connectsoar.backend.dto.UpdateProfileRequest;
import com.connectsoar.backend.dto.UserDto;
import com.connectsoar.backend.enums.AuditAction;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final AuditLogService auditLogService;

    public ProfileService(ProfileRepository profileRepository, AuditLogService auditLogService) {
        this.profileRepository = profileRepository;
        this.auditLogService = auditLogService;
    }

    public UserDto getProfile(UserPrincipal user) {
        log.info("Fetching profile for userId: {}", user.getUserId());
        Profile profile = profileRepository.findById(user.getUserId())
                .orElseGet(() -> {
                    Profile defaultProfile = Profile.builder()
                            .id(user.getUserId())
                            .email(user.getEmail())
                            .name(user.getName() != null ? user.getName() : "")
                            .role(user.getRole())
                            .status(user.getStatus())
                            .resetPassword(user.isResetPassword())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return profileRepository.save(defaultProfile);
                });
        return mapToUserDto(profile);
    }

    public UserDto updateProfile(UserPrincipal user, UpdateProfileRequest request) {
        log.info("Updating self profile for userId: {}", user.getUserId());
        Profile profile = profileRepository.findById(user.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User profile not found", HttpStatus.NOT_FOUND));

        if (request.getFullName() != null) {
            profile.setName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhone(request.getPhoneNumber());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getDesignation() != null) {
            profile.setDesignation(request.getDesignation());
        }

        Profile saved = profileRepository.save(profile);
        return mapToUserDto(saved);
    }

    public UserDto getEmployeeById(String employeeId) {
        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Employee not found with id: " + employeeId, HttpStatus.NOT_FOUND));
        return mapToUserDto(profile);
    }

    public PagedResponse<UserDto> getEmployeesPaged(String search, UserStatus status, String department, int page, int limit) {
        int validatedPage = Math.max(1, page);
        int validatedLimit = Math.min(100, Math.max(1, limit));

        List<Profile> employees = profileRepository.findEmployees(search, status, department, validatedPage, validatedLimit);
        long total = profileRepository.countEmployees(search, status, department);
        int totalPages = (int) Math.ceil((double) total / validatedLimit);

        List<UserDto> dtoList = employees.stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());

        PagedResponse.PaginationMeta meta = PagedResponse.PaginationMeta.builder()
                .page(validatedPage)
                .limit(validatedLimit)
                .total(total)
                .totalPages(totalPages == 0 ? 1 : totalPages)
                .build();

        return PagedResponse.<UserDto>builder()
                .items(dtoList)
                .pagination(meta)
                .build();
    }

    public UserDto updateEmployee(String employeeId, UpdateEmployeeRequest request, String actorUserId) {
        log.info("Admin updating employee: {}", employeeId);
        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Employee not found", HttpStatus.NOT_FOUND));

        if (request.getName() != null) {
            profile.setName(request.getName());
        }
        if (request.getDepartment() != null) {
            profile.setDepartment(request.getDepartment());
        }
        if (request.getDesignation() != null) {
            profile.setDesignation(request.getDesignation());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }
        if (request.getImageUrl() != null) {
            profile.setImageUrl(request.getImageUrl());
        }

        Profile saved = profileRepository.save(profile);

        Map<String, Object> meta = new HashMap<>();
        meta.put("updated_by", actorUserId);
        auditLogService.record(actorUserId, AuditAction.EMPLOYEE_UPDATED, "Profile", employeeId, meta);

        return mapToUserDto(saved);
    }

    public UserDto updateEmployeeStatus(String employeeId, UserStatus status, String actorUserId) {
        log.info("Admin updating employee status: {} -> {}", employeeId, status);
        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Employee not found", HttpStatus.NOT_FOUND));

        UserStatus oldStatus = profile.getStatus();
        profile.setStatus(status);
        Profile saved = profileRepository.save(profile);

        AuditAction action = (status == UserStatus.active) ? AuditAction.EMPLOYEE_ENABLED : AuditAction.EMPLOYEE_DISABLED;
        Map<String, Object> meta = new HashMap<>();
        meta.put("old_status", oldStatus.name());
        meta.put("new_status", status.name());
        auditLogService.record(actorUserId, action, "Profile", employeeId, meta);

        return mapToUserDto(saved);
    }

    public UserDto updateEmployeeRole(String employeeId, Role role, String actorUserId) {
        log.info("Admin updating employee role: {} -> {}", employeeId, role);
        Profile profile = profileRepository.findById(employeeId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Employee not found", HttpStatus.NOT_FOUND));

        if (employeeId.equals(actorUserId) && role != Role.admin) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Admins cannot demote their own role.", HttpStatus.FORBIDDEN);
        }

        Role oldRole = profile.getRole();
        profile.setRole(role);
        Profile saved = profileRepository.save(profile);

        Map<String, Object> meta = new HashMap<>();
        meta.put("old_role", oldRole.name());
        meta.put("new_role", role.name());
        auditLogService.record(actorUserId, AuditAction.ROLE_CHANGED, "Profile", employeeId, meta);

        return mapToUserDto(saved);
    }

    public UserDto mapToUserDto(Profile profile) {
        if (profile == null) return null;
        return UserDto.builder()
                .id(profile.getId())
                .email(profile.getEmail())
                .name(profile.getName())
                .role(profile.getRole() != null ? profile.getRole().name() : "employee")
                .status(profile.getStatus() != null ? profile.getStatus().name() : "active")
                .department(profile.getDepartment())
                .designation(profile.getDesignation())
                .phone(profile.getPhone())
                .imageUrl(profile.getImageUrl())
                .resetPassword(profile.isResetPassword())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
