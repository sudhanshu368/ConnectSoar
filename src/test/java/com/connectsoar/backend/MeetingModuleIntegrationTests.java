package com.connectsoar.backend;

import com.connectsoar.backend.dto.AddParticipantRequest;
import com.connectsoar.backend.dto.CreateMeetingRequest;
import com.connectsoar.backend.dto.JoinCodeRequest;
import com.connectsoar.backend.dto.JoinMeetingRequest;
import com.connectsoar.backend.dto.UpdateMeetingRequest;
import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.enums.ParticipantRole;
import com.connectsoar.backend.enums.ParticipantStatus;
import com.connectsoar.backend.enums.RecurrenceType;
import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.MeetingParticipant;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.MeetingInvitationRepository;
import com.connectsoar.backend.repository.MeetingMessageRepository;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.repository.MeetingSessionRepository;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.PasswordUtil;
import com.connectsoar.backend.service.MeetingChatService;
import com.connectsoar.backend.service.MeetingSessionService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MeetingModuleIntegrationTests {

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
    private MeetingInvitationRepository invitationRepository;

    @Autowired
    private MeetingSessionRepository sessionRepository;

    @Autowired
    private MeetingMessageRepository messageRepository;

    @Autowired
    private MeetingSessionService sessionService;

    @Autowired
    private MeetingChatService chatService;

    @Autowired
    private PasswordUtil passwordUtil;

    private Profile hostUser;
    private Profile participantUser;
    private Profile outsiderUser;
    private Profile adminUser;

    private String hostToken;
    private String participantToken;
    private String outsiderToken;
    private String adminToken;

    @BeforeEach
    void setUp() {
        profileRepository.clear();
        meetingRepository.clear();
        participantRepository.clear();
        invitationRepository.clear();
        sessionRepository.clear();
        messageRepository.clear();

        // 1. Host user
        hostUser = Profile.builder()
                .id("host-" + UUID.randomUUID())
                .email("host@connectsoar.com")
                .name("Meeting Host")
                .role(Role.employee)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("Engineering")
                .designation("Tech Lead")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(hostUser);
        hostToken = "test_token_" + hostUser.getId() + "_" + System.currentTimeMillis();

        // 2. Participant user
        participantUser = Profile.builder()
                .id("participant-" + UUID.randomUUID())
                .email("participant@connectsoar.com")
                .name("Alice Participant")
                .role(Role.employee)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("Product")
                .designation("Product Manager")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(participantUser);
        participantToken = "test_token_" + participantUser.getId() + "_" + System.currentTimeMillis();

        // 3. Outsider user
        outsiderUser = Profile.builder()
                .id("outsider-" + UUID.randomUUID())
                .email("outsider@connectsoar.com")
                .name("Bob Outsider")
                .role(Role.employee)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("Sales")
                .designation("Account Exec")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(outsiderUser);
        outsiderToken = "test_token_" + outsiderUser.getId() + "_" + System.currentTimeMillis();

        // 4. Admin user
        adminUser = Profile.builder()
                .id("admin-" + UUID.randomUUID())
                .email("admin@connectsoar.com")
                .name("Super Admin")
                .role(Role.admin)
                .status(UserStatus.active)
                .resetPassword(false)
                .department("IT")
                .designation("System Administrator")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        profileRepository.save(adminUser);
        adminToken = "test_token_" + adminUser.getId() + "_" + System.currentTimeMillis();
    }

    // =========================================================================
    // 1. Authenticated user can create meeting & 2. Creator automatically becomes HOST
    // =========================================================================
    @Test
    @DisplayName("1 & 2. Authenticated user can create meeting and is automatically assigned HOST role")
    void test1_2_CreateMeetingAndAutoHost() throws Exception {
        CreateMeetingRequest request = CreateMeetingRequest.builder()
                .title("Ad-hoc Architecture Sync")
                .description("Discuss architecture and implementation")
                .meetingType(MeetingType.INSTANT_ROOM)
                .password("secret123")
                .participantUserIds(List.of(participantUser.getId()))
                .participantEmails(List.of("invited.external@example.com"))
                .allowParticipantChat(true)
                .allowScreenSharing(true)
                .muteParticipantsOnEntry(false)
                .allowParticipantVideo(true)
                .allowParticipantAudio(true)
                .isOpenRoom(false)
                .build();

        MvcResult result = mockMvc.perform(post("/api/meetings")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Meeting created successfully"))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.meetingCode").isNotEmpty())
                .andExpect(jsonPath("$.data.meetingUrl").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("Ad-hoc Architecture Sync"))
                .andExpect(jsonPath("$.data.meetingType").value("INSTANT_ROOM"))
                .andExpect(jsonPath("$.data.status").value("LIVE"))
                .andExpect(jsonPath("$.data.host.id").value(hostUser.getId()))
                .andExpect(jsonPath("$.data.host.name").value(hostUser.getName()))
                .andExpect(jsonPath("$.data.host.email").value(hostUser.getEmail()))
                .andExpect(jsonPath("$.data.passwordProtected").value(true))
                .andExpect(jsonPath("$.data.password").value("secret123")) // Plaintext only in create response
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist()) // Never return password_hash
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        String meetingId = root.get("data").get("id").asText();

        // Verify host in participants repository
        MeetingParticipant hostPart = participantRepository.findByMeetingIdAndUserId(meetingId, hostUser.getId()).orElseThrow();
        assertEquals(ParticipantRole.HOST, hostPart.getParticipantRole());

        // Verify password in repository is hashed, not plaintext
        Meeting savedMeeting = meetingRepository.findById(meetingId).orElseThrow();
        assertNotEquals("secret123", savedMeeting.getPasswordHash());
        assertTrue(passwordUtil.matches("secret123", savedMeeting.getPasswordHash()));
    }

    // =========================================================================
    // 3. Host cannot be duplicated & 4. Participant can be added & 5. Duplicate participant ignored safely
    // =========================================================================
    @Test
    @DisplayName("3, 4, 5. Participant management, prevent host duplication, prevent duplicate participants")
    void test3_4_5_ParticipantManagement() throws Exception {
        // Create meeting
        CreateMeetingRequest createReq = CreateMeetingRequest.builder()
                .title("Weekly Tech Sync")
                .meetingType(MeetingType.SCHEDULED_MEETING)
                .scheduledStartTime(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .participantUserIds(List.of(participantUser.getId()))
                .build();

        MvcResult result = mockMvc.perform(post("/api/meetings")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn();

        String meetingId = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("id").asText();

        // 3. Host cannot be added as normal participant (should return 400)
        AddParticipantRequest addHostReq = AddParticipantRequest.builder()
                .userId(hostUser.getId())
                .build();
        mockMvc.perform(post("/api/meetings/" + meetingId + "/participants")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addHostReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        // 4. Add new participant via email
        AddParticipantRequest addOutsiderReq = AddParticipantRequest.builder()
                .email(outsiderUser.getEmail())
                .build();
        mockMvc.perform(post("/api/meetings/" + meetingId + "/participants")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addOutsiderReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(outsiderUser.getId()));

        // 5. Duplicate add does not create duplicate record
        mockMvc.perform(post("/api/meetings/" + meetingId + "/participants")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addOutsiderReq)))
                .andExpect(status().isCreated());

        List<MeetingParticipant> participants = participantRepository.findAllByMeetingId(meetingId);
        long outsiderCount = participants.stream().filter(p -> p.getUserId().equals(outsiderUser.getId())).count();
        assertEquals(1, outsiderCount, "Outsider user should only have 1 participant record");
    }

    // =========================================================================
    // 6. Normal user cannot access another user's private meeting & 7. Admin sees all
    // =========================================================================
    @Test
    @DisplayName("6 & 7. Private meeting access control: unauthorized user rejected (403), Admin can access all")
    void test6_7_AccessControlAndAdminPrivileges() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("PRIV-123-ABC")
                .meetingUrl("https://connectsoar.com/meeting/PRIV-123-ABC")
                .title("Confidential Executive Review")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.SCHEDULED)
                .isOpenRoom(false)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);

        // Host is participant
        participantRepository.save(MeetingParticipant.builder()
                .id(UUID.randomUUID().toString())
                .meetingId(meeting.getId())
                .userId(hostUser.getId())
                .participantRole(ParticipantRole.HOST)
                .build());

        // 6. Outsider cannot access private meeting
        mockMvc.perform(get("/api/meetings/" + meeting.getId())
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // 7. Admin can access private meeting
        mockMvc.perform(get("/api/meetings/" + meeting.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(meeting.getId()));

        // Admin can list all meetings
        mockMvc.perform(get("/api/meetings")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    // =========================================================================
    // 8. My Meeting tab & 9. Upcoming tab & 10. Live tab & 11. Completed tab
    // =========================================================================
    @Test
    @DisplayName("8, 9, 10, 11. Meeting tabs filtering: my, upcoming, live, completed")
    void test8_9_10_11_MeetingTabsFiltering() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        // Meeting 1: Hosted by hostUser, SCHEDULED (upcoming)
        Meeting m1 = Meeting.builder()
                .id("m1-" + UUID.randomUUID())
                .meetingCode("M1-UPC-111")
                .meetingUrl("https://connectsoar.com/meeting/M1-UPC-111")
                .title("Upcoming Roadmap Meeting")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.SCHEDULED)
                .scheduledStartTime(now.plusHours(2))
                .createdAt(now.minusMinutes(10))
                .build();
        meetingRepository.save(m1);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m1.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m1.getId()).userId(participantUser.getId()).participantRole(ParticipantRole.PARTICIPANT).build());

        // Meeting 2: Hosted by outsiderUser, LIVE (participantUser is invited)
        Meeting m2 = Meeting.builder()
                .id("m2-" + UUID.randomUUID())
                .meetingCode("M2-LIV-222")
                .meetingUrl("https://connectsoar.com/meeting/M2-LIV-222")
                .title("Live Incident Triage")
                .hostUserId(outsiderUser.getId())
                .status(MeetingStatus.LIVE)
                .startedAt(now.minusMinutes(15))
                .createdAt(now.minusMinutes(20))
                .build();
        meetingRepository.save(m2);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m2.getId()).userId(outsiderUser.getId()).participantRole(ParticipantRole.HOST).build());
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m2.getId()).userId(participantUser.getId()).participantRole(ParticipantRole.PARTICIPANT).build());

        // Meeting 3: Hosted by hostUser, COMPLETED
        Meeting m3 = Meeting.builder()
                .id("m3-" + UUID.randomUUID())
                .meetingCode("M3-CMP-333")
                .meetingUrl("https://connectsoar.com/meeting/M3-CMP-333")
                .title("Completed Retro")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.COMPLETED)
                .startedAt(now.minusHours(2))
                .endedAt(now.minusHours(1))
                .createdAt(now.minusHours(3))
                .build();
        meetingRepository.save(m3);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m3.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());

        // 8. Tab = "my" for hostUser: returns m1 and m3 (not m2)
        mockMvc.perform(get("/api/meetings?tab=my")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2));

        // Tab = "my" for participantUser: returns 0 because participant is not host
        mockMvc.perform(get("/api/meetings?tab=my")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        // 9. Tab = "upcoming" for participantUser: returns m1
        mockMvc.perform(get("/api/meetings?tab=upcoming")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(m1.getId()));

        // 10. Tab = "live" for participantUser: returns m2
        mockMvc.perform(get("/api/meetings?tab=live")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(m2.getId()));

        // 11. Tab = "completed" for hostUser: returns m3
        mockMvc.perform(get("/api/meetings?tab=completed")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(m3.getId()));
    }

    // =========================================================================
    // 12. Join with correct password & 13. Join with wrong password fails
    // =========================================================================
    @Test
    @DisplayName("12 & 13. Password protected meeting: correct password succeeds, wrong password fails (401)")
    void test12_13_PasswordProtectedMeetingJoin() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("PASS-SEC-999")
                .meetingUrl("https://connectsoar.com/meeting/PASS-SEC-999")
                .title("Protected Strategy Discussion")
                .hostUserId(hostUser.getId())
                .passwordHash(passwordUtil.hashPassword("CorrectPass!"))
                .status(MeetingStatus.LIVE)
                .isOpenRoom(false)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);

        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(participantUser.getId()).build());

        // 13. Wrong password -> 401 UNAUTHORIZED
        JoinMeetingRequest wrongReq = new JoinMeetingRequest("WrongPass!");
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + participantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));

        // 12. Correct password -> 200 OK
        JoinMeetingRequest correctReq = new JoinMeetingRequest("CorrectPass!");
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + participantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(correctReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingToken").isNotEmpty());
    }

    // =========================================================================
    // 14. Host joins without password & 15. Unauthorized user cannot join
    // =========================================================================
    @Test
    @DisplayName("14 & 15. Host can join without password, unauthorized outsider is rejected (403)")
    void test14_15_HostJoinBypassAndOutsiderRejection() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("HOST-BYP-111")
                .meetingUrl("https://connectsoar.com/meeting/HOST-BYP-111")
                .title("Host Bypass Meeting")
                .hostUserId(hostUser.getId())
                .passwordHash(passwordUtil.hashPassword("SuperSecret!"))
                .status(MeetingStatus.SCHEDULED)
                .isOpenRoom(false)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());

        // 14. Host joins without password -> 200 OK and becomes LIVE
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.role", equalToIgnoringCase("host")))
                .andExpect(jsonPath("$.data.status", equalToIgnoringCase("live")));

        // 15. Outsider (not added to participants) cannot join -> 403 FORBIDDEN
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/join")
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // =========================================================================
    // 16, 17, 18, 19. Start and End meeting lifecycle: Host vs Participant permissions
    // =========================================================================
    @Test
    @DisplayName("16, 17, 18, 19. Host can start/end meeting; Participant is rejected (403)")
    void test16_19_StartAndEndMeetingLifecycle() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("LIFECYCLE-123")
                .meetingUrl("https://connectsoar.com/meeting/LIFECYCLE-123")
                .title("Lifecycle Test Meeting")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.SCHEDULED)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(participantUser.getId()).participantRole(ParticipantRole.PARTICIPANT).build());

        // 17. Participant tries to start meeting -> 403 FORBIDDEN
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/start")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // 16. Host starts meeting -> 200 OK, becomes LIVE
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/start")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("LIVE"))
                .andExpect(jsonPath("$.data.startedAt").isNotEmpty());

        // 19. Participant tries to end meeting -> 403 FORBIDDEN
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/end")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // 18. Host ends meeting -> 200 OK, becomes COMPLETED
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/end")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.endedAt").isNotEmpty());
    }

    // =========================================================================
    // 20. Host can remove participant & Host cannot remove themselves
    // =========================================================================
    @Test
    @DisplayName("20. Host can remove participant; Host cannot remove self (403)")
    void test20_RemoveParticipant() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("REM-PART-123")
                .meetingUrl("https://connectsoar.com/meeting/REM-PART-123")
                .title("Removal Test")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.LIVE)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(participantUser.getId()).participantRole(ParticipantRole.PARTICIPANT).status(ParticipantStatus.JOINED).build());

        // Host tries to remove self -> 403 FORBIDDEN
        mockMvc.perform(delete("/api/meetings/" + meeting.getId() + "/participants/" + hostUser.getId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));

        // Host removes participant -> 200 OK
        mockMvc.perform(delete("/api/meetings/" + meeting.getId() + "/participants/" + participantUser.getId())
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        MeetingParticipant removed = participantRepository.findByMeetingIdAndUserId(meeting.getId(), participantUser.getId()).orElseThrow();
        assertEquals(ParticipantStatus.REMOVED, removed.getStatus());
    }

    // =========================================================================
    // 21, 22, 23, 24. Room Permissions enforcement & Mute on entry
    // =========================================================================
    @Test
    @DisplayName("21, 22, 23, 24. Enforce permissions: chat, screen share, camera, mute on entry")
    void test21_24_RoomPermissionsEnforcement() throws Exception {
        CreateMeetingRequest req = CreateMeetingRequest.builder()
                .title("Restricted Webinar")
                .meetingType(MeetingType.INSTANT_ROOM)
                .allowParticipantChat(false)
                .allowScreenSharing(false)
                .allowParticipantVideo(false)
                .allowParticipantAudio(false)
                .muteParticipantsOnEntry(true)
                .participantUserIds(List.of(participantUser.getId()))
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/meetings")
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String meetingId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("data").get("id").asText();

        // Participant joins
        MvcResult joinResult = mockMvc.perform(post("/api/meetings/" + meetingId + "/join")
                        .header("Authorization", "Bearer " + participantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.permissions.canChat").value(false))
                .andExpect(jsonPath("$.data.permissions.canShareScreen").value(false))
                .andExpect(jsonPath("$.data.permissions.canUseCamera").value(false))
                .andExpect(jsonPath("$.data.permissions.canUseMicrophone").value(false))
                .andExpect(jsonPath("$.data.permissions.mutedOnEntry").value(true))
                .andReturn();

        // Participant attempting to send chat when chat is disabled -> 403 FORBIDDEN
        assertThrows(Exception.class, () -> {
            chatService.sendMessage(new com.connectsoar.backend.security.UserPrincipal(participantUser.getId(), participantUser.getEmail(), participantUser.getName(), Role.employee, UserStatus.active, false, "access"), meetingId, "Hello chat!");
        });
    }

    // =========================================================================
    // 18. Join using meeting code
    // =========================================================================
    @Test
    @DisplayName("18 (Prompt Item 18). Join using meeting code via POST /api/meetings/join-code")
    void testJoinUsingMeetingCode() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("ABC-123-XYZ")
                .meetingUrl("https://connectsoar.com/meeting/ABC-123-XYZ")
                .title("Design Sprint")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.LIVE)
                .isOpenRoom(true)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);

        JoinCodeRequest joinCodeReq = new JoinCodeRequest("ABC-123-XYZ", null);

        mockMvc.perform(post("/api/meetings/join-code")
                        .header("Authorization", "Bearer " + participantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinCodeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.meetingCode").value("ABC-123-XYZ"))
                .andExpect(jsonPath("$.data.meetingId").value(meeting.getId()))
                .andExpect(jsonPath("$.data.meetingToken").isNotEmpty());
    }

    // =========================================================================
    // 28 & 29. Search and Pagination
    // =========================================================================
    @Test
    @DisplayName("28 & 29. Search by title/code/host and pagination support")
    void test28_29_SearchAndPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Meeting m = Meeting.builder()
                    .id("meeting-search-" + i)
                    .meetingCode("CODE-" + i + "-ABC")
                    .meetingUrl("https://connectsoar.com/meeting/CODE-" + i + "-ABC")
                    .title("Architecture Review Session " + i)
                    .hostUserId(hostUser.getId())
                    .status(MeetingStatus.SCHEDULED)
                    .scheduledStartTime(LocalDateTime.now().plusDays(i))
                    .createdAt(LocalDateTime.now().minusMinutes(i))
                    .build();
            meetingRepository.save(m);
            participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(m.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());
        }

        // Search for "Review Session 3"
        mockMvc.perform(get("/api/meetings?search=Review Session 3")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Architecture Review Session 3"));

        // Pagination: page 0, size 2
        mockMvc.perform(get("/api/meetings?page=0&size=2")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(3))
                .andExpect(jsonPath("$.data.size").value(2))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    // =========================================================================
    // 31. Dual path compatibility: /api/meetings and /api/v1/meetings
    // =========================================================================
    @Test
    @DisplayName("31. Dual route compatibility: /api/meetings and /api/v1/meetings behave identically")
    void test31_DualRouteCompatibility() throws Exception {
        // GET /api/meetings
        mockMvc.perform(get("/api/meetings")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // GET /api/v1/meetings
        mockMvc.perform(get("/api/v1/meetings")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // 32. Update Meeting via PUT /api/meetings/{meetingId}
    // =========================================================================
    @Test
    @DisplayName("32. Host updates meeting title, description, and permissions via PUT")
    void test32_UpdateMeeting() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("UPD-123-ABC")
                .meetingUrl("https://connectsoar.com/meeting/UPD-123-ABC")
                .title("Initial Title")
                .description("Initial Desc")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.SCHEDULED)
                .allowParticipantChat(true)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());

        UpdateMeetingRequest updateReq = UpdateMeetingRequest.builder()
                .title("Updated Title")
                .description("Updated Desc")
                .allowParticipantChat(false)
                .allowScreenSharing(false)
                .build();

        mockMvc.perform(put("/api/meetings/" + meeting.getId())
                        .header("Authorization", "Bearer " + hostToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Title"))
                .andExpect(jsonPath("$.data.description").value("Updated Desc"))
                .andExpect(jsonPath("$.data.permissions.allowParticipantChat").value(false))
                .andExpect(jsonPath("$.data.permissions.allowScreenSharing").value(false));
    }

    // =========================================================================
    // 33. Meeting Chat messages persistence and retrieval
    // =========================================================================
    @Test
    @DisplayName("33. Chat messages persistence and retrieval via REST /api/meetings/{meetingId}/messages")
    void test33_MeetingChatPersistence() throws Exception {
        Meeting meeting = Meeting.builder()
                .id(UUID.randomUUID().toString())
                .meetingCode("CHAT-123-ABC")
                .meetingUrl("https://connectsoar.com/meeting/CHAT-123-ABC")
                .title("Chat Meeting")
                .hostUserId(hostUser.getId())
                .status(MeetingStatus.LIVE)
                .allowParticipantChat(true)
                .createdAt(LocalDateTime.now())
                .build();
        meetingRepository.save(meeting);
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(hostUser.getId()).participantRole(ParticipantRole.HOST).build());
        participantRepository.save(MeetingParticipant.builder().id(UUID.randomUUID().toString()).meetingId(meeting.getId()).userId(participantUser.getId()).participantRole(ParticipantRole.PARTICIPANT).build());

        // Send chat message
        mockMvc.perform(post("/api/meetings/" + meeting.getId() + "/messages")
                        .header("Authorization", "Bearer " + participantUserToken(participantUser.getId(), participantUser.getEmail(), participantUser.getName()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Hello from participant!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Hello from participant!"));

        // Retrieve messages
        mockMvc.perform(get("/api/meetings/" + meeting.getId() + "/messages")
                        .header("Authorization", "Bearer " + hostToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].message").value("Hello from participant!"));
    }

    private String participantUserToken(String userId, String email, String name) {
        return "test_token_" + userId + "_" + System.currentTimeMillis();
    }
}
