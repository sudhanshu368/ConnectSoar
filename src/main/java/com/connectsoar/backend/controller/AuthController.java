package com.connectsoar.backend.controller;

import com.connectsoar.backend.dto.ApiResponse;
import com.connectsoar.backend.dto.ChangePasswordRequest;
import com.connectsoar.backend.dto.ForgotPasswordRequest;
import com.connectsoar.backend.dto.LoginRequest;
import com.connectsoar.backend.dto.LoginSuccessData;
import com.connectsoar.backend.dto.RefreshTokenData;
import com.connectsoar.backend.dto.RefreshTokenRequest;
import com.connectsoar.backend.dto.UserDto;
import com.connectsoar.backend.security.PublicEndpoint;
import com.connectsoar.backend.security.UserPrincipal;
import com.connectsoar.backend.service.ProfileService;
import com.connectsoar.backend.service.SupabaseAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final SupabaseAuthService supabaseAuthService;
    private final ProfileService profileService;

    public AuthController(SupabaseAuthService supabaseAuthService, ProfileService profileService) {
        this.supabaseAuthService = supabaseAuthService;
        this.profileService = profileService;
    }

    @PublicEndpoint
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginSuccessData>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);
        LoginSuccessData data = supabaseAuthService.login(request, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Login successful.", data));
    }

    @PublicEndpoint
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshTokenData>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);
        RefreshTokenData data = supabaseAuthService.refreshToken(request, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully.", data));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestAttribute(value = "userPrincipal", required = false) UserPrincipal principal) {
        log.info("Logging out user: {}", principal != null ? principal.getUserId() : "unknown");
        return ResponseEntity.ok(ApiResponse.okMessage("Logged out successfully."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(
            @RequestAttribute("userPrincipal") UserPrincipal principal) {
        UserDto userDto = profileService.getProfile(principal);
        return ResponseEntity.ok(ApiResponse.ok(userDto));
    }

    @PublicEndpoint
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestAttribute(value = "userPrincipal", required = false) UserPrincipal principal,
            HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);
        supabaseAuthService.changePassword(request, principal, clientIp);

        Map<String, Object> data = new HashMap<>();
        data.put("reset_password", false);

        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully.", data));
    }

    @PublicEndpoint
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);
        supabaseAuthService.forgotPassword(request, clientIp);
        return ResponseEntity.ok(ApiResponse.okMessage("If an account exists with this email, password reset instructions have been sent."));
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }
}
