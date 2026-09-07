package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.enums.ParticipantRole;
import com.connectsoar.backend.enums.ParticipantStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParticipantResponse {

    private String id;

    @JsonProperty("meetingId")
    private String meetingId;

    @JsonProperty("userId")
    private String userId;

    private String name;
    private String email;
    private String imageUrl;

    @Builder.Default
    private ParticipantRole participantRole = ParticipantRole.PARTICIPANT;

    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.INVITED;

    private MeetingPermission permission;

    private LocalDateTime invitedAt;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
