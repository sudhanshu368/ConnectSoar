package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.UserDto;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.security.PublicEndpoint;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/users", "/api/users"})
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final ProfileService profileService;

    public UserController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @RequestAttribute(value = "userPrincipal", required = false) UserPrincipal principal) {
        log.info("Fetching all users list");
        List<UserDto> users = profileService.getAllUsers();
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("items", users);
        data.put("total", users.size());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(
            @PathVariable("userId") String userId,
            @RequestAttribute(value = "userPrincipal", required = false) UserPrincipal principal) {
        log.info("Fetching user by id: {}", userId);
        UserDto user = profileService.getEmployeeById(userId);
        return ResponseEntity.ok(ApiResponse.ok(user));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @PathVariable("userId") String userId,
            @RequestBody Map<String, String> body,
            @RequestAttribute(value = "userPrincipal", required = false) UserPrincipal principal) {
        String roleStr = body.get("role");
        String actorUserId = (principal != null) ? principal.getUserId() : "system";
        Role role = Role.fromString(roleStr);
        UserDto updated = profileService.updateEmployeeRole(userId, role, actorUserId);
        return ResponseEntity.ok(ApiResponse.ok("User role updated successfully.", updated));
    }
}
