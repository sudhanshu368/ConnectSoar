package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.enums.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingResponse {

    private String id;

    @JsonProperty("meetingCode")
    private String meetingCode;

    @JsonProperty("meetingUrl")
    private String meetingUrl;

    private String title;
    private String description;

    @JsonProperty("meetingType")
    private MeetingType meetingType;

    private MeetingStatus status;

    private MeetingHostDto host;

    private List<ParticipantResponse> participants;

    @JsonProperty("participantCount")
    private Integer participantCount;

    @JsonProperty("scheduledStartTime")
    private LocalDateTime scheduledStartTime;

    @JsonProperty("scheduledEndTime")
    private LocalDateTime scheduledEndTime;

    @JsonProperty("durationMinutes")
    private Integer durationMinutes;

    private String timezone;

    @JsonProperty("reminderMinutes")
    private Integer reminderMinutes;

    @JsonProperty("recurrenceType")
    private RecurrenceType recurrenceType;

    private MeetingPermissionsDto permissions;

    @JsonProperty("passwordProtected")
    private Boolean passwordProtected;

    @JsonProperty("password")
    private String password;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("endedAt")
    private LocalDateTime endedAt;

    // Backward compatibility getters
    public String getHostId() {
        return host != null ? host.getId() : null;
    }

    public String getHostName() {
        return host != null ? host.getName() : null;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledStartTime;
    }

    public int getParticipantsCount() {
        return participantCount != null ? participantCount : 0;
    }
}
