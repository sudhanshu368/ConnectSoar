package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.MeetingPermission;
import com.connectsoar.backend.enums.ParticipantRole;
import com.connectsoar.backend.enums.ParticipantStatus;
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
public class MeetingParticipant {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("user_id")
    private String userId;

    @Builder.Default
    @JsonProperty("participant_role")
    private ParticipantRole participantRole = ParticipantRole.PARTICIPANT;

    @Builder.Default
    private ParticipantStatus status = ParticipantStatus.INVITED;

    @JsonProperty("invited_at")
    private LocalDateTime invitedAt;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("left_at")
    private LocalDateTime leftAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    // Backward compatibility helper
    public MeetingPermission getPermission() {
        if (participantRole == ParticipantRole.HOST) {
            return MeetingPermission.host;
        }
        return MeetingPermission.participant;
    }

    public void setPermission(MeetingPermission permission) {
        if (permission == MeetingPermission.host) {
            this.participantRole = ParticipantRole.HOST;
        } else {
            this.participantRole = ParticipantRole.PARTICIPANT;
        }
    }

    public static class MeetingParticipantBuilder {
        public MeetingParticipantBuilder permission(MeetingPermission permission) {
            if (permission == MeetingPermission.host) {
                return this.participantRole(ParticipantRole.HOST);
            } else {
                return this.participantRole(ParticipantRole.PARTICIPANT);
            }
        }
    }
}
