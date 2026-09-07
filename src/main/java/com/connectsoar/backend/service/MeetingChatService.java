package com.connectsoar.backend.service;

import com.connectsoar.backend.dto.ChatMessageDto;
import com.connectsoar.backend.enums.ErrorCode;
import com.connectsoar.backend.exception.ApiException;
import com.connectsoar.backend.model.Meeting;
import com.connectsoar.backend.model.MeetingMessage;
import com.connectsoar.backend.model.Profile;
import com.connectsoar.backend.repository.MeetingMessageRepository;
import com.connectsoar.backend.repository.MeetingParticipantRepository;
import com.connectsoar.backend.repository.MeetingRepository;
import com.connectsoar.backend.repository.ProfileRepository;
import com.connectsoar.backend.security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MeetingChatService {

    private final MeetingMessageRepository messageRepository;
    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final ProfileRepository profileRepository;

    public MeetingChatService(MeetingMessageRepository messageRepository,
                              MeetingRepository meetingRepository,
                              MeetingParticipantRepository participantRepository,
                              ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.meetingRepository = meetingRepository;
        this.participantRepository = participantRepository;
        this.profileRepository = profileRepository;
    }

    public ChatMessageDto sendMessage(UserPrincipal user, String meetingId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Chat message cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        if (content.length() > 2000) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Chat message exceeds maximum length of 2000 characters.", HttpStatus.BAD_REQUEST);
        }

        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        if (!user.isAdmin() && !meeting.getHostUserId().equals(user.getUserId())) {
            boolean isParticipant = participantRepository.findByMeetingIdAndUserId(meetingId, user.getUserId()).isPresent();
            if (!isParticipant && !meeting.isOpenRoom()) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have access to this meeting's chat.", HttpStatus.FORBIDDEN);
            }

            if (!meeting.isAllowParticipantChat()) {
                throw new ApiException(ErrorCode.FORBIDDEN, "Participant chat is disabled for this meeting.", HttpStatus.FORBIDDEN);
            }
        }

        Profile profile = profileRepository.findById(user.getUserId()).orElse(null);
        String senderName = profile != null ? profile.getName() : user.getName();
        String senderEmail = profile != null ? profile.getEmail() : user.getEmail();

        MeetingMessage message = MeetingMessage.builder()
                .id(UUID.randomUUID().toString())
                .meetingId(meetingId)
                .senderUserId(user.getUserId())
                .senderName(senderName)
                .senderEmail(senderEmail)
                .message(content.trim())
                .createdAt(LocalDateTime.now())
                .build();

        MeetingMessage saved = messageRepository.save(message);

        return ChatMessageDto.builder()
                .id(saved.getId())
                .meetingId(saved.getMeetingId())
                .senderUserId(saved.getSenderUserId())
                .senderName(saved.getSenderName())
                .senderEmail(saved.getSenderEmail())
                .message(saved.getMessage())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    public List<ChatMessageDto> getMessages(UserPrincipal user, String meetingId) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Meeting not found", HttpStatus.NOT_FOUND));

        if (!user.isAdmin() && !meeting.getHostUserId().equals(user.getUserId())) {
            boolean isParticipant = participantRepository.findByMeetingIdAndUserId(meetingId, user.getUserId()).isPresent();
            if (!isParticipant && !meeting.isOpenRoom()) {
                throw new ApiException(ErrorCode.ACCESS_DENIED, "You do not have access to this meeting's chat history.", HttpStatus.FORBIDDEN);
            }
        }

        return messageRepository.findAllByMeetingIdOrderByCreatedAtAsc(meetingId).stream()
                .map(m -> ChatMessageDto.builder()
                        .id(m.getId())
                        .meetingId(m.getMeetingId())
                        .senderUserId(m.getSenderUserId())
                        .senderName(m.getSenderName())
                        .senderEmail(m.getSenderEmail())
                        .message(m.getMessage())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
