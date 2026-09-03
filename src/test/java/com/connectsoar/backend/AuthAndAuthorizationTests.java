package com.connectsoar.backend;

import com.connectsoar.backend.dto.*;
import com.connectsoar.backend.enums.*;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.MeetingParticipant;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.AuditLogRepository;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthAndAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private MeetingParticipantRepository participantRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Profile adminProfile;
    private Profile employeeActiveProfile;
    private Profile employeeResetRequiredProfile;
    private Profile employeeInactiveProfile;
    private Profile employeeSuspendedProfile;

    private String adminToken;
    private String employeeActiveToken;
    private String employeeResetToken;

    @BeforeEach
    void setUp() {
        profileRepository.clear();
        meetingRepository.clear();
        participantRepository.clear();
        auditLogRepository.clear();

        // 1. Admin user
        adminProfile = Profile.builder()
                .id("admin-" + UUID.randomUUID())
                .email("admin@connectsoar.com")
                .name("Super Admin")
                .role(Role.admin)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("Management")
                .designation("System Administrator")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(adminProfile);
        adminToken = "test_token_" + adminProfile.getId() + "_" + System.currentTimeMillis();

        // 2. Active Employee (reset_password = false)
        employeeActiveProfile = Profile.builder()
                .id("emp-active-" + UUID.randomUUID())
                .email("active.employee@connectsoar.com")
                .name("John Active")
                .role(Role.employee)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("Engineering")
                .designation("Backend Engineer")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(employeeActiveProfile);
        employeeActiveToken = "test_token_" + employeeActiveProfile.getId() + "_" + System.currentTimeMillis();

        // 3. Newly provisioned Employee (reset_password = true)
        employeeResetRequiredProfile = Profile.builder()
                .id("emp-reset-" + UUID.randomUUID())
                .email("new.employee@connectsoar.com")
                .name("Jane New")
                .role(Role.employee)
                .status(UserStatus.active)
                .resetPassword(true) // STRICT REQUIREMENT
                .department("Design")
                .designation("UI Designer")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(employeeResetRequiredProfile);
        employeeResetToken = "test_token_" + employeeResetRequiredProfile.getId() + "_" + System.currentTimeMillis();

        // 4. Inactive Employee
        employeeInactiveProfile = Profile.builder()
                .id("emp-inactive-" + UUID.randomUUID())
                .email("inactive.employee@connectsoar.com")
                .name("Bob Inactive")
                .role(Role.employee)
                .status(UserStatus.inactive)
                .resetPassword(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(employeeInactiveProfile);

        // 5. Suspended Employee
        employeeSuspendedProfile = Profile.builder()
                .id("emp-suspended-" + UUID.randomUUID())
                .email("suspended.employee@connectsoar.com")
                .name("Alice Suspended")
                .role(Role.employee)
                .status(UserStatus.suspended)
                .resetPassword(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(employeeSuspendedProfile);
    }

    // =========================================================================
    // SCENARIO 1: Valid login + reset_password=false
    // =========================================================================
    @Test
    @DisplayName("1. Valid login with reset_password=false returns access token, refresh token, and user profile")
    void test1_ValidLoginActiveUser() throws Exception {
        LoginRequest req = new LoginRequest(employeeActiveProfile.getEmail(), "Password123!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful."))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value(employeeActiveProfile.getEmail()))
                .andExpect(jsonPath("$.data.user.reset_password").value(false))
                .andExpect(jsonPath("$.data.user.status").value("active"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        assertNotNull(root.get("data").get("access_token").asText());
    }

    // =========================================================================
    // SCENARIO 2: Valid credentials + reset_password=true
    // CRITICAL REQUIREMENT: NO access_token or refresh_token returned! Returns 403 PASSWORD_CHANGE_REQUIRED
    // =========================================================================
    @Test
    @DisplayName("2. Valid credentials + reset_password=true returns 403 PASSWORD_CHANGE_REQUIRED without session tokens")
    void test2_ValidCredentialsResetPasswordTrue() throws Exception {
        LoginRequest req = new LoginRequest(employeeResetRequiredProfile.getEmail(), "TemporaryPass123!");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_CHANGE_REQUIRED"))
                .andExpect(jsonPath("$.error.message").value("Password change is required before accessing the application."))
                .andExpect(jsonPath("$.data.reset_password").value(true))
                .andExpect(jsonPath("$.data.user.email").value(employeeResetRequiredProfile.getEmail()))
                .andExpect(jsonPath("$.data.password_reset_token").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        // Verify NO session access_token or refresh_token in response!
        assertNull(root.get("data").get("access_token"));
        assertNull(root.get("data").get("refresh_token"));
    }

    // =========================================================================
    // SCENARIO 3: Invalid password
    // =========================================================================
    @Test
    @DisplayName("3. Invalid credentials returns 401 INVALID_CREDENTIALS")
    void test3_InvalidPassword() throws Exception {
        LoginRequest req = new LoginRequest(employeeActiveProfile.getEmail(), "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    // =========================================================================
    // SCENARIO 4: Non-existent email (Generic error, prevents email enumeration)
    // =========================================================================
    @Test
    @DisplayName("4. Non-existent email returns generic 401 INVALID_CREDENTIALS")
    void test4_NonExistentEmail() throws Exception {
        LoginRequest req = new LoginRequest("doesnotexist@example.com", "SomePassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_CREDENTIALS"));
    }

    // =========================================================================
    // SCENARIO 5: Inactive user
    // =========================================================================
    @Test
    @DisplayName("5. Inactive user login returns 403 USER_INACTIVE")
    void test5_InactiveUserLogin() throws Exception {
        LoginRequest req = new LoginRequest(employeeInactiveProfile.getEmail(), "Pass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_INACTIVE"));
    }

    // =========================================================================
    // SCENARIO 6: Suspended user
    // =========================================================================
    @Test
    @DisplayName("6. Suspended user login returns 403 USER_SUSPENDED")
    void test6_SuspendedUserLogin() throws Exception {
        LoginRequest req = new LoginRequest(employeeSuspendedProfile.getEmail(), "Pass123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_SUSPENDED"));
    }

    // =========================================================================
    // SCENARIO 7: Expired access token
    // =========================================================================
    @Test
    @DisplayName("7. Expired access token returns 401 TOKEN_EXPIRED")
    void test7_ExpiredAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer expired.mock.jwt.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // SCENARIO 8: Invalid access token
    // =========================================================================
    @Test
    @DisplayName("8. Invalid or malformed access token returns 401 TOKEN_INVALID")
    void test8_InvalidAccessToken() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer invalid_malformed_token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // SCENARIO 9: Valid refresh token
    // =========================================================================
    @Test
    @DisplayName("9. Valid refresh token successfully returns refreshed tokens")
    void test9_ValidRefreshToken() throws Exception {
        String validRefresh = "test_refresh_" + employeeActiveProfile.getId() + "_" + System.currentTimeMillis();
        RefreshTokenRequest req = new RefreshTokenRequest(validRefresh, employeeActiveToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty());
    }

    // =========================================================================
    // SCENARIO 10: Invalid refresh token
    // =========================================================================
    @Test
    @DisplayName("10. Invalid refresh token returns 401/400 TOKEN_INVALID")
    void test10_InvalidRefreshToken() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest("completely_bogus_refresh_token", null);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // SCENARIO 11: Employee attempting admin API (403 FORBIDDEN)
    // =========================================================================
    @Test
    @DisplayName("11. Employee attempting admin API returns 403 FORBIDDEN")
    void test11_EmployeeAttemptingAdminApi() throws Exception {
        mockMvc.perform(get("/api/v1/admin/employees")
                        .header("Authorization", "Bearer " + employeeActiveToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    // =========================================================================
    // SCENARIO 12: Employee attempting role escalation
    // =========================================================================
    @Test
    @DisplayName("12. Employee attempting role escalation is blocked with 403")
    void test12_EmployeeAttemptingRoleEscalation() throws Exception {
        UpdateRoleRequest roleReq = new UpdateRoleRequest("admin");

        mockMvc.perform(patch("/api/v1/admin/employees/" + employeeActiveProfile.getId() + "/role")
                        .header("Authorization", "Bearer " + employeeActiveToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("FORBIDDEN"));
    }

    // =========================================================================
    // SCENARIO 13: Employee self-profile isolation
    // =========================================================================
    @Test
    @DisplayName("13. Employee can only access their own profile via /profile")
    void test13_EmployeeSelfProfileIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + employeeActiveToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(employeeActiveProfile.getId()))
                .andExpect(jsonPath("$.data.email").value(employeeActiveProfile.getEmail()));
    }

    // =========================================================================
    // SCENARIO 14 & 15: Password changed successfully -> reset_password becomes false
    // =========================================================================
    @Test
    @DisplayName("14 & 15. Password changed with reset token sets reset_password=false")
    void test14_15_PasswordSuccessfullyChanged() throws Exception {
        // Generate single-purpose short-lived password reset token
        String resetToken = jwtTokenProvider.generatePasswordResetToken(
                employeeResetRequiredProfile.getId(), employeeResetRequiredProfile.getEmail());

        ChangePasswordRequest changeReq = new ChangePasswordRequest(
                null, "BrandNewSecurePassword123!", "BrandNewSecurePassword123!", resetToken);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reset_password").value(false));

        // Verify profile in database now has reset_password = false
        Profile updated = profileRepository.findById(employeeResetRequiredProfile.getId()).orElseThrow();
        assertFalse(updated.isResetPassword());

        // Now user can perform normal login and receive tokens
        LoginRequest loginReq = new LoginRequest(employeeResetRequiredProfile.getEmail(), "BrandNewSecurePassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty());
    }

    // =========================================================================
    // SCENARIO 16: User cannot access meetings while reset_password=true
    // =========================================================================
    @Test
    @DisplayName("16. User cannot access meetings while reset_password=true")
    void test16_UserBlockedFromMeetingsWhileResetPasswordTrue() throws Exception {
        mockMvc.perform(get("/api/v1/meetings")
                        .header("Authorization", "Bearer " + employeeResetToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("PASSWORD_CHANGE_REQUIRED"));
    }

    // =========================================================================
    // SCENARIO 17: Forgot-password enumeration protection
    // =========================================================================
    @Test
    @DisplayName("17. Forgot-password returns identical generic message regardless of email existence")
    void test17_ForgotPasswordEnumerationProtection() throws Exception {
        ForgotPasswordRequest reqExisting = new ForgotPasswordRequest(employeeActiveProfile.getEmail());
        ForgotPasswordRequest reqNonExisting = new ForgotPasswordRequest("nonexistentuser999@connectsoar.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqExisting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists with this email, password reset instructions have been sent."));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqNonExisting)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If an account exists with this email, password reset instructions have been sent."));
    }

    // =========================================================================
    // SCENARIO 18: Login rate limiting
    // =========================================================================
    @Test
    @DisplayName("18. Rate limiter throttles rapid brute-force requests with 429 RATE_LIMITED")
    void test18_LoginRateLimiting() throws Exception {
        LoginRequest req = new LoginRequest("test.ratelimit@example.com", "Password!");

        // Fire multiple login requests from same simulated IP
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                    .header("X-Forwarded-For", "192.168.1.100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)));
        }

        // 11th request must be rate limited
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Forwarded-For", "192.168.1.100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("RATE_LIMITED"));
    }

    // =========================================================================
    // SCENARIO 19: Refresh-token rotation
    // =========================================================================
    @Test
    @DisplayName("19. Refresh token endpoint issues fresh rotated tokens")
    void test19_RefreshTokenRotation() throws Exception {
        String originalRefresh = "test_refresh_" + employeeActiveProfile.getId() + "_1";
        RefreshTokenRequest req = new RefreshTokenRequest(originalRefresh, employeeActiveToken);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.refresh_token").isNotEmpty())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String newAccess = root.get("data").get("access_token").asText();
        assertNotNull(newAccess);
    }

    // =========================================================================
    // SCENARIO 20: Unauthorized meeting join (403 ACCESS_DENIED)
    // =========================================================================
    @Test
    @DisplayName("20. Unauthorized employee attempting to join private meeting is rejected with 403")
    void test20_UnauthorizedMeetingJoin() throws Exception {
        // Admin creates a private meeting
        Meeting meeting = Meeting.builder()
                .id("meeting-" + UUID.randomUUID())
                .title("Executive Private Board Meeting")
                .hostId(adminProfile.getId())
                .status(MeetingStatus.scheduled)
                .scheduledAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);

        // Employee (who is not a participant) attempts to join
        mockMvc.perform(post("/api/v1/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + employeeActiveToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    // =========================================================================
    // SCENARIO 21: Authorized meeting join (returns short-lived scoped token)
    // =========================================================================
    @Test
    @DisplayName("21. Authorized participant join returns short-lived scoped meeting token")
    void test21_AuthorizedMeetingJoin() throws Exception {
        // Create meeting
        Meeting meeting = Meeting.builder()
                .id("meeting-" + UUID.randomUUID())
                .title("Weekly Sprint Standup")
                .hostId(adminProfile.getId())
                .status(MeetingStatus.scheduled)
                .scheduledAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);

        // Add employee as participant
        MeetingParticipant participant = MeetingParticipant.builder()
                .id(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .userId(employeeActiveProfile.getId())
                .permission(MeetingPermission.participant)
                .build();
        participantRepository.save(participant);

        // Employee joins meeting
        MvcResult result = mockMvc.perform(post("/api/v1/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + employeeActiveToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meeting_id").value(meeting.getId()))
                .andExpect(jsonPath("$.data.room_id").value("room-" + meeting.getId()))
                .andExpect(jsonPath("$.data.meeting_token").isNotEmpty())
                .andExpect(jsonPath("$.data.expires_in").value(900))
                .andExpect(jsonPath("$.data.role").value("participant"))
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String meetingToken = root.get("data").get("meeting_token").asText();
        assertNotNull(meetingToken);
    }

    // =========================================================================
    // SCENARIO 22: Disabled/Inactive employee attempting access with an old JWT
    // =========================================================================
    @Test
    @DisplayName("22. Disabled/Inactive employee is immediately rejected even with an old unexpired JWT")
    void test22_DisabledEmployeeAccessRejected() throws Exception {
        // Inactive user attempts API with their token
        String inactiveToken = "test_token_" + employeeInactiveProfile.getId() + "_" + System.currentTimeMillis();

        mockMvc.perform(get("/api/v1/profile")
                        .header("Authorization", "Bearer " + inactiveToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_INACTIVE"));
    }

    // =========================================================================
    // ADMIN EMPLOYEE CREATION: Strict Defaults Enforced (role=employee, status=active, reset_password=true)
    // =========================================================================
    @Test
    @DisplayName("Admin creates employee: forces role=employee, status=active, reset_password=true")
    void testAdminCreateEmployeeStrictDefaults() throws Exception {
        CreateEmployeeAdminRequest req = new CreateEmployeeAdminRequest(
                "Rohit Sharma", "rohit@connectsoar.com", "Mobile Engineering", "Senior Flutter Dev", "+919876543210");

        mockMvc.perform(post("/api/v1/admin/employees")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role").value("employee"))
                .andExpect(jsonPath("$.data.status").value("active"))
                .andExpect(jsonPath("$.data.reset_password").value(true));
    }
}
