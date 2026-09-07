package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.AddParticipantRequest;
import com.connectsoar.backend.dto.CreateMeetingRequest;
import com.connectsoar.backend.dto.MeetingHostDto;
import com.connectsoar.backend.dto.MeetingJoinResponse;
import com.connectsoar.backend.dto.MeetingPageData;
import com.connectsoar.backend.dto.MeetingPermissionsDto;
import com.connectsoar.backend.dto.MeetingResponse;
import com.connectsoar.backend.dto.ParticipantResponse;
import com.connectsoar.backend.dto.UpdateMeetingRequest;
import com.connectsoar.backend.dto.UpdateParticipantRequest;
import com.connectsoar.backend.enums.AuditAction;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.InvitationStatus;
import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.enums.ParticipantRole;
import com.connectsoar.backend.enums.ParticipantStatus;
import com.connectsoar.backend.enums.RecurrenceType;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.MeetingInvitation;
import com.connectsoar.backend.model.MeetingParticipant;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.MeetingInvitationRepository;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.JwtTokenProvider;
import com.connectsoar.backend.security.PasswordUtil;
import com.connectsoar.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final MeetingInvitationRepository invitationRepository;
    private final ProfileRepository profileRepository;
    private final MeetingSessionService sessionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;
    private final PasswordUtil passwordUtil;

    @Value("${app.meeting.base-url:https://connectsoar.com/meeting}")
    private String meetingBaseUrl;

    public MeetingService(MeetingRepository meetingRepository,
                          MeetingParticipantRepository participantRepository,
                          MeetingInvitationRepository invitationRepository,
                          ProfileRepository profileRepository,
                          MeetingSessionService sessionService,
                          JwtTokenProvider jwtTokenProvider,
                          AuditLogService auditLogService,
                          PasswordUtil passwordUtil) {
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
        this.invitationRepository = invitationRepository;
        this.profileRepository = profileRepository;
        this.sessionService = sessionService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogService = auditLogService;
        this.passwordUtil = passwordUtil;
    }

    public MeetingResponse createMeeting(UserPrincipal user, CreateMeetingRequest request) {
        log.info("Creating meeting by user: {}", user.getUserId());

        if (request == null) {
            request = new CreateMeetingRequest();
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            String userName = user.getName() != null && !user.getName().isBlank() ? user.getName() : "Instant";
            request.setTitle(userName + "'s Meeting");
        }

        String meetingId = UUID.randomUUID().toString();
        String meetingCode = passwordUtil.generateMeetingCode();
        String meetingUrl = meetingBaseUrl + "/" + meetingCode;
        LocalDateTime now = LocalDateTime.now();

        MeetingType meetingType = request.getMeetingType() != null ? request.getMeetingType() : MeetingType.INSTANT_ROOM;
        MeetingStatus initialStatus = (meetingType == MeetingType.INSTANT_ROOM) ? MeetingStatus.LIVE : MeetingStatus.SCHEDULED;

        LocalDateTime startTime = request.getScheduledStartTime();
        int duration = request.getDurationMinutes() != null && request.getDurationMinutes() > 0 ? request.getDurationMinutes() : 45;
        LocalDateTime endTime = null;

        if (meetingType == MeetingType.SCHEDULED_MEETING) {
            if (startTime == null) {
                startTime = now;
            }
            endTime = startTime.plusMinutes(duration);
        }

        String passwordHash = passwordUtil.hashPassword(request.getPassword());

        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .meetingCode(meetingCode)
                .meetingUrl(meetingUrl)
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .meetingType(meetingType)
                .hostUserId(user.getUserId())
                .passwordHash(passwordHash)
                .status(initialStatus)
                .scheduledStartTime(startTime)
                .scheduledEndTime(endTime)
                .durationMinutes(duration)
                .timezone(request.getTimezone() != null ? request.getTimezone() : "Asia/Kolkata")
                .reminderMinutes(request.getReminderMinutes() != null ? request.getReminderMinutes() : 15)
                .recurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : RecurrenceType.NONE)
                .allowParticipantChat(request.isAllowParticipantChat())
                .allowScreenSharing(request.isAllowScreenSharing())
                .muteParticipantsOnEntry(request.isMuteParticipantsOnEntry())
                .allowParticipantVideo(request.isAllowParticipantVideo())
                .allowParticipantAudio(request.isAllowParticipantAudio())
                .isOpenRoom(request.isOpenRoom())
                .createdAt(now)
                .updatedAt(now)
                .startedAt(meetingType == MeetingType.INSTANT_ROOM ? now : null)
                .build();

        Meeting saved = meetingRepository.save(meeting);

        // Host is automatically added as participant
        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .id(UUID.randomUUID().toString())
                .meetingId(meetingId)
                .userId(user.getUserId())
                .participantRole(ParticipantRole.HOST)
                .status(meetingType == MeetingType.INSTANT_ROOM ? ParticipantStatus.JOINED : ParticipantStatus.ACCEPTED)
                .invitedAt(now)
                .joinedAt(meetingType == MeetingType.INSTANT_ROOM ? now : null)
                .createdAt(now)
                .updatedAt(now)
                .build();
        participantRepository.save(hostParticipant);

        // Process invited participants
        Set<String> addedUserIds = new HashSet<>();
        addedUserIds.add(user.getUserId());

        if (request.getParticipantUserIds() != null) {
            for (String targetUserId : request.getParticipantUserIds()) {
                if (targetUserId != null && !addedUserIds.contains(targetUserId)) {
                    Optional<Profile> targetProfile = profileRepository.findById(targetUserId);
                    if (targetProfile.isPresent()) {
                        MeetingParticipant participant = MeetingParticipant.builder()
                                .id(UUID.randomUUID().toString())
                                .meetingId(meetingId)
                                .userId(targetUserId)
                                .participantRole(ParticipantRole.PARTICIPANT)
                                .status(ParticipantStatus.INVITED)
                                .invitedAt(now)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        participantRepository.save(participant);
                        addedUserIds.add(targetUserId);
                    }
                }
            }
        }

        if (request.getParticipantEmails() != null) {
            for (String email : request.getParticipantEmails()) {
                if (email != null && !email.isBlank()) {
                    String cleanEmail = email.trim();
                    Optional<Profile> existingProfile = profileRepository.findByEmail(cleanEmail);

                    String targetUserId = existingProfile.map(Profile::getId).orElse(null);
                    if (targetUserId != null && !addedUserIds.contains(targetUserId)) {
                        MeetingParticipant participant = MeetingParticipant.builder()
                                .id(UUID.randomUUID().toString())
                                .meetingId(meetingId)
                                .userId(targetUserId)
                                .participantRole(ParticipantRole.PARTICIPANT)
                                .status(ParticipantStatus.INVITED)
                                .invitedAt(now)
                                .createdAt(now)
                                .updatedAt(now)
                                .build();
                        participantRepository.save(participant);
                        addedUserIds.add(targetUserId);
                    }

                    // Save invitation record
                    MeetingInvitation invitation = MeetingInvitation.builder()
                            .id(UUID.randomUUID().toString())
                            .meetingId(meetingId)
                            .email(cleanEmail)
                            .userId(targetUserId)
                            .status(InvitationStatus.PENDING)
                            .token(UUID.randomUUID().toString())
                            .expiresAt(startTime != null ? startTime.plusDays(1) : now.plusDays(1))
                            .createdAt(now)
                            .updatedAt(now)
                            .build();
                    invitationRepository.save(invitation);
                }
            }
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", saved.getTitle());
        meta.put("meetingCode", saved.getMeetingCode());
        meta.put("meetingType", saved.getMeetingType().name());
        auditLogService.record(user.getUserId(), AuditAction.MEETING_CREATED, "Meeting", meetingId, meta);

        MeetingResponse response = mapToMeetingResponse(saved);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            response.setPassword(request.getPassword()); // Return original plain password ONLY upon creation
        }

        return response;
    }

    public MeetingPageData<MeetingResponse> getMeetingsPaged(UserPrincipal user,
                                                             String tab,
                                                             String search,
                                                             String statusStr,
                                                             int page,
                                                             int size) {
        log.info("Fetching meetings paged for user: {}, tab: {}, search: {}", user.getUserId(), tab, search);

        MeetingStatus filterStatus = null;
        if (statusStr != null && !statusStr.isBlank()) {
            filterStatus = MeetingStatus.fromString(statusStr);
        }

        List<MeetingParticipant> userMemberships = participantRepository.findAllByUserId(user.getUserId());
        Set<String> participantMeetingIds = userMemberships.stream().map(MeetingParticipant::getMeetingId).collect(Collectors.toSet());

        Map<String, Profile> profileMap = profileRepository.findAll().stream()
                .collect(Collectors.toMap(Profile::getId, p -> p, (a, b) -> a));

        List<Meeting> meetings = meetingRepository.findFiltered(
                tab, search, filterStatus, user.getUserId(), user.isAdmin(), participantMeetingIds, profileMap, page, size);

        long totalElements = meetingRepository.countFiltered(
                tab, search, filterStatus, user.getUserId(), user.isAdmin(), participantMeetingIds, profileMap);

        int totalPages = (int) Math.ceil((double) totalElements / Math.max(1, size));
        if (totalPages == 0) totalPages = 1;

        List<MeetingResponse> dtoList = meetings.stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());

        return MeetingPageData.<MeetingResponse>builder()
                .content(dtoList)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    public List<MeetingResponse> getMeetingsForUser(UserPrincipal user) {
        MeetingPageData<MeetingResponse> paged = getMeetingsPaged(user, null, null, null, 0, 100);
        return paged.getContent();
    }

    public MeetingResponse getMeetingById(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyMeetingAccess(user, meeting);

        return mapToMeetingResponse(meeting);
    }

    public MeetingResponse updateMeeting(UserPrincipal user, String meetingId, UpdateMeetingRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            meeting.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getScheduledStartTime() != null) {
            meeting.setScheduledStartTime(request.getScheduledStartTime());
        }
        if (request.getDurationMinutes() != null && request.getDurationMinutes() > 0) {
            meeting.setDurationMinutes(request.getDurationMinutes());
            if (meeting.getScheduledStartTime() != null) {
                meeting.setScheduledEndTime(meeting.getScheduledStartTime().plusMinutes(request.getDurationMinutes()));
            }
        }
        if (request.getTimezone() != null) {
            meeting.setTimezone(request.getTimezone());
        }
        if (request.getReminderMinutes() != null) {
            meeting.setReminderMinutes(request.getReminderMinutes());
        }
        if (request.getRecurrenceType() != null) {
            meeting.setRecurrenceType(request.getRecurrenceType());
        }
        if (request.getAllowParticipantChat() != null) {
            meeting.setAllowParticipantChat(request.getAllowParticipantChat());
        }
        if (request.getAllowScreenSharing() != null) {
            meeting.setAllowScreenSharing(request.getAllowScreenSharing());
        }
        if (request.getMuteParticipantsOnEntry() != null) {
            meeting.setMuteParticipantsOnEntry(request.getMuteParticipantsOnEntry());
        }
        if (request.getAllowParticipantVideo() != null) {
            meeting.setAllowParticipantVideo(request.getAllowParticipantVideo());
        }
        if (request.getAllowParticipantAudio() != null) {
            meeting.setAllowParticipantAudio(request.getAllowParticipantAudio());
        }
        if (request.getIsOpenRoom() != null) {
            meeting.setIsOpenRoom(request.getIsOpenRoom());
        }
        if (request.getStatus() != null) {
            MeetingStatus newStatus = MeetingStatus.fromString(request.getStatus());
            meeting.setStatus(newStatus);
            if (newStatus == MeetingStatus.LIVE && meeting.getStartedAt() == null) {
                meeting.setStartedAt(LocalDateTime.now());
            } else if (newStatus == MeetingStatus.COMPLETED || newStatus == MeetingStatus.CANCELLED) {
                if (meeting.getEndedAt() == null) {
                    meeting.setEndedAt(LocalDateTime.now());
                }
            }
        }

        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting saved = meetingRepository.save(meeting);

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", saved.getTitle());
        meta.put("status", saved.getStatus().name());
        auditLogService.record(user.getUserId(), AuditAction.MEETING_UPDATED, "Meeting", meetingId, meta);

        return mapToMeetingResponse(saved);
    }

    public void deleteMeeting(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        meeting.setStatus(MeetingStatus.CANCELLED);
        meeting.setEndedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        meetingRepository.save(meeting);

        auditLogService.record(user.getUserId(), AuditAction.MEETING_CANCELLED, "Meeting", meetingId, null);
    }

    public MeetingResponse startMeeting(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot start a cancelled meeting.", HttpStatus.BAD_REQUEST);
        }
        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Cannot start an already completed meeting.", HttpStatus.BAD_REQUEST);
        }

        meeting.setStatus(MeetingStatus.LIVE);
        meeting.setStartedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting saved = meetingRepository.save(meeting);

        return mapToMeetingResponse(saved);
    }

    public MeetingResponse endMeeting(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        meeting.setStatus(MeetingStatus.COMPLETED);
        meeting.setEndedAt(LocalDateTime.now());
        meeting.setUpdatedAt(LocalDateTime.now());
        Meeting saved = meetingRepository.save(meeting);

        // Mark active participants as LEFT
        List<MeetingParticipant> participants = participantRepository.findAllByMeetingId(meetingId);
        LocalDateTime now = LocalDateTime.now();
        for (MeetingParticipant p : participants) {
            if (p.getStatus() == ParticipantStatus.JOINED) {
                p.setStatus(ParticipantStatus.LEFT);
                p.setLeftAt(now);
                participantRepository.save(p);
            }
        }

        return mapToMeetingResponse(saved);
    }

    public MeetingJoinResponse joinMeeting(UserPrincipal user, String meetingId, String rawPassword) {
        log.info("User {} requesting to join meeting {}", user.getUserId(), meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        return executeJoin(user, meeting, rawPassword);
    }

    public MeetingJoinResponse joinMeeting(UserPrincipal user, String meetingId) {
        return joinMeeting(user, meetingId, null);
    }

    public MeetingJoinResponse joinByCode(UserPrincipal user, String meetingCode, String rawPassword) {
        log.info("User {} requesting to join with code {}", user.getUserId(), meetingCode);

        Meeting meeting = meetingRepository.findByMeetingCode(meetingCode)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found with code: " + meetingCode, HttpStatus.NOT_FOUND));

        return executeJoin(user, meeting, rawPassword);
    }

    public void leaveMeeting(UserPrincipal user, String meetingId) {
        log.info("User {} leaving meeting {}", user.getUserId(), meetingId);

        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingIdAndUserId(meetingId, user.getUserId());
        if (participantOpt.isPresent()) {
            MeetingParticipant p = participantOpt.get();
            p.setStatus(ParticipantStatus.LEFT);
            p.setLeftAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            participantRepository.save(p);
        }

        sessionService.markUserLeft(meetingId, user.getUserId());
    }

    private MeetingJoinResponse executeJoin(UserPrincipal user, Meeting meeting, String rawPassword) {
        if (meeting.getStatus() == MeetingStatus.CANCELLED) {
            throw new ApiException(ErrorCode.MEETING_CANCELLED, "This meeting has been cancelled.", HttpStatus.BAD_REQUEST);
        }
        if (meeting.getStatus() == MeetingStatus.COMPLETED) {
            throw new ApiException(ErrorCode.MEETING_ALREADY_ENDED, "This meeting has already ended.", HttpStatus.BAD_REQUEST);
        }

        boolean isHost = meeting.getHostUserId().equals(user.getUserId());
        boolean isAdmin = user.isAdmin();
        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingIdAndUserId(meeting.getId(), user.getUserId());

        if (!isHost && !isAdmin && !meeting.isOpenRoom() && participantOpt.isEmpty()) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have permission to access this meeting", HttpStatus.FORBIDDEN);
        }

        // Validate password if configured (host and admin bypass password requirement)
        if (meeting.getPasswordHash() != null && !isHost && !isAdmin) {
            if (rawPassword == null || !passwordUtil.matches(rawPassword, meeting.getPasswordHash())) {
                throw new ApiException(ErrorCode.INVALID_MEETING_PASSWORD, "Invalid meeting password.", HttpStatus.UNAUTHORIZED);
            }
        }

        // Auto-transition SCHEDULED to LIVE if host joins
        if (isHost && meeting.getStatus() == MeetingStatus.SCHEDULED) {
            meeting.setStatus(MeetingStatus.LIVE);
            meeting.setStartedAt(LocalDateTime.now());
            meetingRepository.save(meeting);
        }

        // Update participant record
        LocalDateTime now = LocalDateTime.now();
        ParticipantRole role = isHost ? ParticipantRole.HOST : ParticipantRole.PARTICIPANT;
        MeetingParticipant participant;

        if (participantOpt.isPresent()) {
            participant = participantOpt.get();
            participant.setStatus(ParticipantStatus.JOINED);
            participant.setJoinedAt(now);
            participant.setUpdatedAt(now);
        } else {
            participant = MeetingParticipant.builder()
                    .id(UUID.randomUUID().toString())
                    .meetingId(meeting.getId())
                    .userId(user.getUserId())
                    .participantRole(role)
                    .status(ParticipantStatus.JOINED)
                    .invitedAt(now)
                    .joinedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
        }
        participantRepository.save(participant);

        String sessionId = UUID.randomUUID().toString();
        sessionService.createOrUpdateSession(meeting.getId(), user.getUserId(), sessionId, meeting.isMuteParticipantsOnEntry());

        MeetingPermission tokenPermission = isHost ? MeetingPermission.host : (isAdmin ? MeetingPermission.co_host : MeetingPermission.participant);
        String roomId = "room-" + meeting.getId();
        String meetingToken = jwtTokenProvider.generateMeetingToken(user.getUserId(), user.getEmail(), meeting.getId(), roomId, tokenPermission);

        MeetingPermissionsDto permissionsDto = MeetingPermissionsDto.builder()
                .canChat(isHost || isAdmin || meeting.isAllowParticipantChat())
                .canShareScreen(isHost || isAdmin || meeting.isAllowScreenSharing())
                .canUseCamera(isHost || isAdmin || meeting.isAllowParticipantVideo())
                .canUseMicrophone(isHost || isAdmin || meeting.isAllowParticipantAudio())
                .mutedOnEntry(meeting.isMuteParticipantsOnEntry())
                .allowParticipantChat(meeting.isAllowParticipantChat())
                .allowScreenSharing(meeting.isAllowScreenSharing())
                .muteParticipantsOnEntry(meeting.isMuteParticipantsOnEntry())
                .allowParticipantVideo(meeting.isAllowParticipantVideo())
                .allowParticipantAudio(meeting.isAllowParticipantAudio())
                .isOpenRoom(meeting.isOpenRoom())
                .build();

        return MeetingJoinResponse.builder()
                .meetingId(meeting.getId())
                .meetingCode(meeting.getMeetingCode())
                .meetingUrl(meeting.getMeetingUrl())
                .roomId(roomId)
                .role(role.name())
                .status(meeting.getStatus().name())
                .permissions(permissionsDto)
                .sessionId(sessionId)
                .meetingToken(meetingToken)
                .expiresIn(900L)
                .build();
    }

    public ParticipantResponse addParticipant(UserPrincipal user, String meetingId, AddParticipantRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        Profile targetUser = null;
        if (request.getUserId() != null && !request.getUserId().isBlank()) {
            targetUser = profileRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Target user not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
            targetUser = profileRepository.findByEmail(request.getEmail().trim())
                    .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Target user not found with email: " + request.getEmail(), HttpStatus.NOT_FOUND));
        } else {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Either userId or email must be provided.", HttpStatus.BAD_REQUEST);
        }

        if (meeting.getHostUserId().equals(targetUser.getId())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "The host is already a participant.", HttpStatus.BAD_REQUEST);
        }

        Optional<MeetingParticipant> existing = participantRepository.findByMeetingIdAndUserId(meetingId, targetUser.getId());
        MeetingParticipant saved;
        LocalDateTime now = LocalDateTime.now();

        if (existing.isPresent()) {
            saved = existing.get();
            saved.setStatus(ParticipantStatus.INVITED);
            saved.setUpdatedAt(now);
            saved = participantRepository.save(saved);
        } else {
            MeetingParticipant newParticipant = MeetingParticipant.builder()
                    .id(UUID.randomUUID().toString())
                    .meetingId(meetingId)
                    .userId(targetUser.getId())
                    .participantRole(ParticipantRole.PARTICIPANT)
                    .status(ParticipantStatus.INVITED)
                    .invitedAt(now)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            saved = participantRepository.save(newParticipant);
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("target_user_id", targetUser.getId());
        auditLogService.record(user.getUserId(), AuditAction.PARTICIPANT_ADDED, "MeetingParticipant", saved.getId(), meta);

        return ParticipantResponse.builder()
                .id(saved.getId())
                .meetingId(meetingId)
                .userId(targetUser.getId())
                .name(targetUser.getName())
                .email(targetUser.getEmail())
                .imageUrl(targetUser.getImageUrl())
                .participantRole(saved.getParticipantRole())
                .status(saved.getStatus())
                .permission(saved.getPermission())
                .invitedAt(saved.getInvitedAt())
                .joinedAt(saved.getJoinedAt())
                .leftAt(saved.getLeftAt())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    public List<ParticipantResponse> getParticipants(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyMeetingAccess(user, meeting);

        List<MeetingParticipant> participants = participantRepository.findAllByMeetingId(meetingId);
        return participants.stream().map(p -> {
            Optional<Profile> userProfile = profileRepository.findById(p.getUserId());
            return ParticipantResponse.builder()
                    .id(p.getId())
                    .meetingId(meetingId)
                    .userId(p.getUserId())
                    .name(userProfile.map(Profile::getName).orElse("Unknown"))
                    .email(userProfile.map(Profile::getEmail).orElse(""))
                    .imageUrl(userProfile.map(Profile::getImageUrl).orElse(null))
                    .participantRole(p.getParticipantRole())
                    .status(p.getStatus())
                    .permission(p.getPermission())
                    .invitedAt(p.getInvitedAt())
                    .joinedAt(p.getJoinedAt())
                    .leftAt(p.getLeftAt())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public ParticipantResponse updateParticipantPermission(UserPrincipal user, String meetingId, String targetUserId, UpdateParticipantRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        MeetingParticipant participant = participantRepository.findByMeetingIdAndUserId(meetingId, targetUserId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Participant not found in meeting", HttpStatus.NOT_FOUND));

        MeetingPermission newPermission = MeetingPermission.fromString(request.getPermission());
        participant.setPermission(newPermission);
        participant.setUpdatedAt(LocalDateTime.now());
        MeetingParticipant saved = participantRepository.save(participant);

        Optional<Profile> profileOpt = profileRepository.findById(targetUserId);
        return ParticipantResponse.builder()
                .id(saved.getId())
                .meetingId(meetingId)
                .userId(targetUserId)
                .name(profileOpt.map(Profile::getName).orElse("Unknown"))
                .email(profileOpt.map(Profile::getEmail).orElse(""))
                .imageUrl(profileOpt.map(Profile::getImageUrl).orElse(null))
                .participantRole(saved.getParticipantRole())
                .status(saved.getStatus())
                .permission(saved.getPermission())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    public void removeParticipant(UserPrincipal user, String meetingId, String targetUserId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        if (meeting.getHostUserId().equals(targetUserId)) {
            throw new ApiException(ErrorCode.CANNOT_REMOVE_HOST, "Cannot remove the meeting host.", HttpStatus.FORBIDDEN);
        }

        Optional<MeetingParticipant> partOpt = participantRepository.findByMeetingIdAndUserId(meetingId, targetUserId);
        if (partOpt.isPresent()) {
            MeetingParticipant p = partOpt.get();
            p.setStatus(ParticipantStatus.REMOVED);
            p.setLeftAt(LocalDateTime.now());
            p.setUpdatedAt(LocalDateTime.now());
            participantRepository.save(p);
        }

        sessionService.markUserLeft(meetingId, targetUserId);

        Map<String, Object> meta = new HashMap<>();
        meta.put("removed_user_id", targetUserId);
        auditLogService.record(user.getUserId(), AuditAction.PARTICIPANT_REMOVED, "MeetingParticipant", meetingId, meta);
    }

    private void verifyMeetingAccess(UserPrincipal user, Meeting meeting) {
        if (user.isAdmin()) return;
        if (meeting.getHostUserId().equals(user.getUserId())) return;
        if (meeting.isOpenRoom()) return;

        boolean isParticipant = participantRepository.findByMeetingIdAndUserId(meeting.getId(), user.getUserId()).isPresent();
        if (!isParticipant) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have permission to access this meeting", HttpStatus.FORBIDDEN);
        }
    }

    private void verifyHostOrAdmin(UserPrincipal user, Meeting meeting) {
        if (user.isAdmin()) return;
        if (meeting.getHostUserId().equals(user.getUserId())) return;

        throw new ApiException(ErrorCode.FORBIDDEN, "You must be the meeting host or an administrator to perform this operation.", HttpStatus.FORBIDDEN);
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting) {
        Profile hostProfile = profileRepository.findById(meeting.getHostUserId()).orElse(null);
        MeetingHostDto hostDto = MeetingHostDto.builder()
                .id(meeting.getHostUserId())
                .name(hostProfile != null ? hostProfile.getName() : "Host")
                .email(hostProfile != null ? hostProfile.getEmail() : "")
                .imageUrl(hostProfile != null ? hostProfile.getImageUrl() : null)
                .build();

        MeetingPermissionsDto permissionsDto = MeetingPermissionsDto.builder()
                .allowParticipantChat(meeting.isAllowParticipantChat())
                .allowScreenSharing(meeting.isAllowScreenSharing())
                .muteParticipantsOnEntry(meeting.isMuteParticipantsOnEntry())
                .allowParticipantVideo(meeting.isAllowParticipantVideo())
                .allowParticipantAudio(meeting.isAllowParticipantAudio())
                .isOpenRoom(meeting.isOpenRoom())
                .build();

        int count = participantRepository.countByMeetingId(meeting.getId());

        List<ParticipantResponse> participants = participantRepository.findAllByMeetingId(meeting.getId()).stream()
                .map(p -> {
                    Profile pProfile = profileRepository.findById(p.getUserId()).orElse(null);
                    return ParticipantResponse.builder()
                            .id(p.getId())
                            .meetingId(p.getMeetingId())
                            .userId(p.getUserId())
                            .name(pProfile != null ? pProfile.getName() : "Participant")
                            .email(pProfile != null ? pProfile.getEmail() : "")
                            .imageUrl(pProfile != null ? pProfile.getImageUrl() : null)
                            .participantRole(p.getParticipantRole())
                            .status(p.getStatus())
                            .permission(p.getPermission())
                            .invitedAt(p.getInvitedAt())
                            .joinedAt(p.getJoinedAt())
                            .leftAt(p.getLeftAt())
                            .createdAt(p.getCreatedAt())
                            .build();
                }).collect(Collectors.toList());

        return MeetingResponse.builder()
                .id(meeting.getId())
                .meetingCode(meeting.getMeetingCode())
                .meetingUrl(meeting.getMeetingUrl())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .meetingType(meeting.getMeetingType())
                .status(meeting.getStatus())
                .host(hostDto)
                .participants(participants)
                .participantCount(count)
                .scheduledStartTime(meeting.getScheduledStartTime())
                .scheduledEndTime(meeting.getScheduledEndTime())
                .durationMinutes(meeting.getDurationMinutes())
                .timezone(meeting.getTimezone())
                .reminderMinutes(meeting.getReminderMinutes())
                .recurrenceType(meeting.getRecurrenceType())
                .permissions(permissionsDto)
                .passwordProtected(meeting.getPasswordHash() != null)
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .startedAt(meeting.getStartedAt())
                .endedAt(meeting.getEndedAt())
                .build();
    }
}
