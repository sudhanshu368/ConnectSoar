package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.InvitationStatus;
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
public class MeetingInvitation {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    private String email;

    @JsonProperty("user_id")
    private String userId;

    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    private String token;

    @JsonProperty("expires_at")
    private LocalDateTime expiresAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
