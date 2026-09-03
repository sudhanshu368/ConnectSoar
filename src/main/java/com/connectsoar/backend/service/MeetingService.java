package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.AddParticipantRequest;
import com.connectsoar.backend.dto.CreateMeetingRequest;
import com.connectsoar.backend.dto.MeetingJoinResponse;
import com.connectsoar.backend.dto.MeetingResponse;
import com.connectsoar.backend.dto.ParticipantResponse;
import com.connectsoar.backend.dto.UpdateMeetingRequest;
import com.connectsoar.backend.dto.UpdateParticipantRequest;
import com.connectsoar.backend.enums.AuditAction;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.MeetingParticipant;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.JwtTokenProvider;
import com.connectsoar.backend.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditLogService auditLogService;

    public MeetingService(MeetingRepository meetingRepository,
                          MeetingParticipantRepository participantRepository,
                          ProfileRepository profileRepository,
                          JwtTokenProvider jwtTokenProvider,
                          AuditLogService auditLogService) {
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
        this.profileRepository = profileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditLogService = auditLogService;
    }

    public MeetingResponse createMeeting(UserPrincipal user, CreateMeetingRequest request) {
        log.info("Creating meeting by user: {}", user.getUserId());

        String meetingId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Meeting meeting = Meeting.builder()
                .id(meetingId)
                .organizationId(request.getOrganizationId())
                .title(request.getTitle())
                .description(request.getDescription())
                .hostId(user.getUserId())
                .status(MeetingStatus.scheduled)
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Meeting saved = meetingRepository.save(meeting);

        // Host is automatically added as host participant
        MeetingParticipant hostParticipant = MeetingParticipant.builder()
                .id(UUID.randomUUID().toString())
                .meetingId(meetingId)
                .userId(user.getUserId())
                .permission(MeetingPermission.host)
                .createdAt(now)
                .updatedAt(now)
                .build();
        participantRepository.save(hostParticipant);

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", saved.getTitle());
        meta.put("host_id", user.getUserId());
        auditLogService.record(user.getUserId(), AuditAction.MEETING_CREATED, "Meeting", meetingId, meta);

        return mapToMeetingResponse(saved, user.getName(), 1);
    }

    public List<MeetingResponse> getMeetingsForUser(UserPrincipal user) {
        log.info("Fetching meetings for user: {}", user.getUserId());

        if (user.isAdmin()) {
            return meetingRepository.findAll().stream()
                    .map(m -> mapToMeetingResponse(m, getHostName(m.getHostId()), participantRepository.countByMeetingId(m.getId())))
                    .collect(Collectors.toList());
        }

        List<MeetingParticipant> memberships = participantRepository.findAllByUserId(user.getUserId());
        List<String> meetingIds = memberships.stream().map(MeetingParticipant::getMeetingId).collect(Collectors.toList());

        List<Meeting> meetings = meetingRepository.findByIds(meetingIds);
        return meetings.stream()
                .map(m -> mapToMeetingResponse(m, getHostName(m.getHostId()), participantRepository.countByMeetingId(m.getId())))
                .collect(Collectors.toList());
    }

    public MeetingResponse getMeetingById(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyMeetingAccess(user, meeting);

        return mapToMeetingResponse(meeting, getHostName(meeting.getHostId()), participantRepository.countByMeetingId(meetingId));
    }

    public MeetingResponse updateMeeting(UserPrincipal user, String meetingId, UpdateMeetingRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        if (request.getTitle() != null) {
            meeting.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            meeting.setDescription(request.getDescription());
        }
        if (request.getScheduledAt() != null) {
            meeting.setScheduledAt(request.getScheduledAt());
        }
        if (request.getStatus() != null) {
            meeting.setStatus(MeetingStatus.fromString(request.getStatus()));
            if (meeting.getStatus() == MeetingStatus.ongoing && meeting.getStartedAt() == null) {
                meeting.setStartedAt(LocalDateTime.now());
            } else if (meeting.getStatus() == MeetingStatus.completed || meeting.getStatus() == MeetingStatus.cancelled) {
                meeting.setEndedAt(LocalDateTime.now());
            }
        }

        Meeting saved = meetingRepository.save(meeting);

        Map<String, Object> meta = new HashMap<>();
        meta.put("title", saved.getTitle());
        meta.put("status", saved.getStatus().name());
        auditLogService.record(user.getUserId(), AuditAction.MEETING_UPDATED, "Meeting", meetingId, meta);

        return mapToMeetingResponse(saved, getHostName(saved.getHostId()), participantRepository.countByMeetingId(meetingId));
    }

    public void deleteMeeting(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        meeting.setStatus(MeetingStatus.cancelled);
        meeting.setEndedAt(LocalDateTime.now());
        meetingRepository.save(meeting);

        participantRepository.deleteByMeetingId(meetingId);

        auditLogService.record(user.getUserId(), AuditAction.MEETING_CANCELLED, "Meeting", meetingId, null);
    }

    public MeetingJoinResponse joinMeeting(UserPrincipal user, String meetingId) {
        log.info("User {} requesting to join meeting {}", user.getUserId(), meetingId);

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        if (meeting.getStatus() == MeetingStatus.cancelled) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "This meeting has been cancelled.", HttpStatus.BAD_REQUEST);
        }
        if (meeting.getStatus() == MeetingStatus.completed) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "This meeting has already ended.", HttpStatus.BAD_REQUEST);
        }

        Optional<MeetingParticipant> participantOpt = participantRepository.findByMeetingIdAndUserId(meetingId, user.getUserId());
        MeetingPermission permission;

        if (meeting.getHostId().equals(user.getUserId())) {
            permission = MeetingPermission.host;
        } else if (participantOpt.isPresent()) {
            permission = participantOpt.get().getPermission();
        } else if (user.isAdmin()) {
            permission = MeetingPermission.co_host;
        } else {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "You are not authorized to join this meeting.", HttpStatus.FORBIDDEN);
        }

        String roomId = "room-" + meetingId;
        String meetingToken = jwtTokenProvider.generateMeetingToken(user.getUserId(), user.getEmail(), meetingId, roomId, permission);

        return MeetingJoinResponse.builder()
                .meetingId(meetingId)
                .roomId(roomId)
                .meetingToken(meetingToken)
                .expiresIn(900)
                .role(permission.name())
                .build();
    }

    public ParticipantResponse addParticipant(UserPrincipal user, String meetingId, AddParticipantRequest request) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        Profile targetUser = profileRepository.findById(request.getUserId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "Target user not found", HttpStatus.NOT_FOUND));

        MeetingPermission permission = MeetingPermission.fromString(request.getPermission());

        Optional<MeetingParticipant> existing = participantRepository.findByMeetingIdAndUserId(meetingId, request.getUserId());
        MeetingParticipant saved;
        if (existing.isPresent()) {
            saved = existing.get();
            saved.setPermission(permission);
            saved.setUpdatedAt(LocalDateTime.now());
            participantRepository.save(saved);
        } else {
            MeetingParticipant newParticipant = MeetingParticipant.builder()
                    .id(UUID.randomUUID().toString())
                    .meetingId(meetingId)
                    .userId(request.getUserId())
                    .permission(permission)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            saved = participantRepository.save(newParticipant);
        }

        Map<String, Object> meta = new HashMap<>();
        meta.put("target_user_id", request.getUserId());
        meta.put("permission", permission.name());
        auditLogService.record(user.getUserId(), AuditAction.PARTICIPANT_ADDED, "MeetingParticipant", saved.getId(), meta);

        return ParticipantResponse.builder()
                .id(saved.getId())
                .meetingId(meetingId)
                .userId(targetUser.getId())
                .name(targetUser.getName())
                .email(targetUser.getEmail())
                .permission(saved.getPermission())
                .createdAt(saved.getCreatedAt())
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
                    .permission(p.getPermission())
                    .createdAt(p.getCreatedAt())
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

        Map<String, Object> meta = new HashMap<>();
        meta.put("target_user_id", targetUserId);
        meta.put("new_permission", newPermission.name());
        auditLogService.record(user.getUserId(), AuditAction.PARTICIPANT_UPDATED, "MeetingParticipant", saved.getId(), meta);

        Optional<Profile> profileOpt = profileRepository.findById(targetUserId);
        return ParticipantResponse.builder()
                .id(saved.getId())
                .meetingId(meetingId)
                .userId(targetUserId)
                .name(profileOpt.map(Profile::getName).orElse("Unknown"))
                .email(profileOpt.map(Profile::getEmail).orElse(""))
                .permission(saved.getPermission())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public void removeParticipant(UserPrincipal user, String meetingId, String targetUserId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        verifyHostOrAdmin(user, meeting);

        if (meeting.getHostId().equals(targetUserId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot remove the meeting host.", HttpStatus.FORBIDDEN);
        }

        participantRepository.deleteByMeetingIdAndUserId(meetingId, targetUserId);

        Map<String, Object> meta = new HashMap<>();
        meta.put("removed_user_id", targetUserId);
        auditLogService.record(user.getUserId(), AuditAction.PARTICIPANT_REMOVED, "MeetingParticipant", meetingId, meta);
    }

    private void verifyMeetingAccess(UserPrincipal user, Meeting meeting) {
        if (user.isAdmin()) return;
        if (meeting.getHostId().equals(user.getUserId())) return;
        boolean isParticipant = participantRepository.findByMeetingIdAndUserId(meeting.getId(), user.getUserId()).isPresent();
        if (!isParticipant) {
            throw new ApiException(ErrorCode.ACCESS_DENIED, "Access denied to meeting.", HttpStatus.FORBIDDEN);
        }
    }

    private void verifyHostOrAdmin(UserPrincipal user, Meeting meeting) {
        if (user.isAdmin()) return;
        if (meeting.getHostId().equals(user.getUserId())) return;
        Optional<MeetingParticipant> part = participantRepository.findByMeetingIdAndUserId(meeting.getId(), user.getUserId());
        if (part.isPresent() && part.get().getPermission() == MeetingPermission.co_host) return;

        throw new ApiException(ErrorCode.FORBIDDEN, "You must be host, co-host, or admin to perform this operation.", HttpStatus.FORBIDDEN);
    }

    private String getHostName(String hostId) {
        return profileRepository.findById(hostId).map(Profile::getName).orElse("Host");
    }

    private MeetingResponse mapToMeetingResponse(Meeting meeting, String hostName, int count) {
        return MeetingResponse.builder()
                .id(meeting.getId())
                .organizationId(meeting.getOrganizationId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .hostId(meeting.getHostId())
                .hostName(hostName)
                .status(meeting.getStatus())
                .scheduledAt(meeting.getScheduledAt())
                .startedAt(meeting.getStartedAt())
                .endedAt(meeting.getEndedAt())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .participantsCount(count)
                .build();
    }
}
