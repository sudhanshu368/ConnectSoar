package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.enums.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMeetingRequest {

    @NotBlank(message = "Meeting title is required")
    private String title;

    private String description;

    @Builder.Default
    @JsonProperty("meetingType")
    private MeetingType meetingType = MeetingType.INSTANT_ROOM;

    @JsonProperty("scheduledStartTime")
    private LocalDateTime scheduledStartTime;

    @JsonProperty("durationMinutes")
    private Integer durationMinutes;

    private String timezone;

    @Builder.Default
    @JsonProperty("reminderMinutes")
    private Integer reminderMinutes = 15;

    @Builder.Default
    @JsonProperty("recurrenceType")
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    private String password;

    @JsonProperty("participantUserIds")
    private List<String> participantUserIds;

    @JsonProperty("participantEmails")
    private List<String> participantEmails;

    @Builder.Default
    @JsonProperty("allowParticipantChat")
    private boolean allowParticipantChat = true;

    @Builder.Default
    @JsonProperty("allowScreenSharing")
    private boolean allowScreenSharing = true;

    @Builder.Default
    @JsonProperty("muteParticipantsOnEntry")
    private boolean muteParticipantsOnEntry = false;

    @Builder.Default
    @JsonProperty("allowParticipantVideo")
    private boolean allowParticipantVideo = true;

    @Builder.Default
    @JsonProperty("allowParticipantAudio")
    private boolean allowParticipantAudio = true;

    @Builder.Default
    @JsonProperty("isOpenRoom")
    private boolean isOpenRoom = false;

    // Backward compatibility fields
    @JsonProperty("scheduled_at")
    private LocalDateTime scheduledAt;

    @JsonProperty("organization_id")
    private String organizationId;

    public LocalDateTime getScheduledStartTime() {
        if (scheduledStartTime != null) {
            return scheduledStartTime;
        }
        return scheduledAt;
    }
}
