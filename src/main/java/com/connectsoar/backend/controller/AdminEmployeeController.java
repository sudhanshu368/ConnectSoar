package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.CreateEmployeeAdminRequest;
import com.connectsoar.backend.dto.PagedResponse;
import com.connectsoar.backend.dto.UpdateEmployeeRequest;
import com.connectsoar.backend.dto.UpdateRoleRequest;
import com.connectsoar.backend.dto.UpdateStatusRequest;
import com.connectsoar.backend.dto.UserDto;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;
import com.connectsoar.backend.security.RequireRole;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.ProfileService;
import com.connectsoar.backend.service.SupabaseAuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequireRole(Role.admin)
public class AdminEmployeeController {

    private static final Logger log = LoggerFactory.getLogger(AdminEmployeeController.class);

    private final SupabaseAuthService supabaseAuthService;
    private final ProfileService profileService;

    public AdminEmployeeController(SupabaseAuthService supabaseAuthService, ProfileService profileService) {
        this.supabaseAuthService = supabaseAuthService;
        this.profileService = profileService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDto>> createEmployee(
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody CreateEmployeeAdminRequest request) {
        log.info("Admin {} creating new employee: {}", principal.getUserId(), request.getEmail());
        UserDto employee = supabaseAuthService.createEmployeeByAdmin(request, principal.getUserId());
        return new ResponseEntity<>(ApiResponse.ok("Employee created successfully.", employee), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserDto>>> listEmployees(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "20") int limit,
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "department", required = false) String department) {
        UserStatus userStatus = (status != null && !status.isBlank()) ? UserStatus.fromString(status) : null;
        PagedResponse<UserDto> response = profileService.getEmployeesPaged(search, userStatus, department, page, limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<UserDto>> getEmployee(
            @PathVariable("employeeId") String employeeId) {
        UserDto employee = profileService.getEmployeeById(employeeId);
        return ResponseEntity.ok(ApiResponse.ok(employee));
    }

    @PatchMapping("/{employeeId}")
    public ResponseEntity<ApiResponse<UserDto>> updateEmployee(
            @PathVariable("employeeId") String employeeId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @RequestBody UpdateEmployeeRequest request) {
        UserDto updated = profileService.updateEmployee(employeeId, request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Employee updated successfully.", updated));
    }

    @PatchMapping("/{employeeId}/status")
    public ResponseEntity<ApiResponse<UserDto>> updateEmployeeStatus(
            @PathVariable("employeeId") String employeeId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody UpdateStatusRequest request) {
        UserStatus status = UserStatus.fromString(request.getStatus());
        UserDto updated = profileService.updateEmployeeStatus(employeeId, status, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Employee status updated successfully.", updated));
    }

    @PatchMapping("/{employeeId}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateEmployeeRole(
            @PathVariable("employeeId") String employeeId,
            @RequestAttribute("userPrincipal") UserPrincipal principal,
            @Valid @RequestBody UpdateRoleRequest request) {
        Role role = Role.fromString(request.getRole());
        UserDto updated = profileService.updateEmployeeRole(employeeId, role, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok("Employee role updated successfully.", updated));
    }
}
