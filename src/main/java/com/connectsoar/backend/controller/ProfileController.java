package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.AvatarUploadRequest;
import com.connectsoar.backend.dto.AvatarUploadResponse;
import com.connectsoar.backend.dto.UpdateProfileRequest;
import com.connectsoar.backend.dto.UserDto;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.ProfileService;
import com.connectsoar.backend.service.StorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;
    private final StorageService storageService;

    public ProfileController(ProfileService profileService, StorageService storageService) {
        this.profileService = profileService;
        this.storageService = storageService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getProfile(
            @RequestAttribute("userPrincipal") UserPrincipal userPrincipal) {
        UserDto response = profileService.getProfile(userPrincipal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @RequestAttribute("userPrincipal") UserPrincipal userPrincipal,
            @RequestBody UpdateProfileRequest request) {
        UserDto response = profileService.updateProfile(userPrincipal, request);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully.", response));
    }

    @PostMapping("/avatar/upload-url")
    public ResponseEntity<ApiResponse<AvatarUploadResponse>> getAvatarUploadUrl(
            @RequestAttribute("userPrincipal") UserPrincipal userPrincipal,
            @Valid @RequestBody AvatarUploadRequest request) {
        AvatarUploadResponse response = storageService.generateAvatarUploadUrl(userPrincipal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
