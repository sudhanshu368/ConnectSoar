package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.RecurrenceType;
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
public class UpdateMeetingRequest {

    private String title;
    private String description;

    @JsonProperty("scheduledStartTime")
    private LocalDateTime scheduledStartTime;

    @JsonProperty("durationMinutes")
    private Integer durationMinutes;

    private String timezone;

    @JsonProperty("reminderMinutes")
    private Integer reminderMinutes;

    @JsonProperty("recurrenceType")
    private RecurrenceType recurrenceType;

    private String status;

    @JsonProperty("allowParticipantChat")
    private Boolean allowParticipantChat;

    @JsonProperty("allowScreenSharing")
    private Boolean allowScreenSharing;

    @JsonProperty("muteParticipantsOnEntry")
    private Boolean muteParticipantsOnEntry;

    @JsonProperty("allowParticipantVideo")
    private Boolean allowParticipantVideo;

    @JsonProperty("allowParticipantAudio")
    private Boolean allowParticipantAudio;

    @JsonProperty("isOpenRoom")
    private Boolean isOpenRoom;

    // Backward compatibility
    @JsonProperty("scheduled_at")
    private LocalDateTime scheduledAt;

    public LocalDateTime getScheduledStartTime() {
        if (scheduledStartTime != null) {
            return scheduledStartTime;
        }
        return scheduledAt;
    }
}
