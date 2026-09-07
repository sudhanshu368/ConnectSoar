package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingType;
import com.connectsoar.backend.enums.RecurrenceType;
import com.fasterxml.jackson.annotation.JsonAlias;
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
public class CreateMeetingRequest {

    @JsonAlias({"title", "meetingTitle", "meeting_title", "name"})
    private String title;

    private String description;

    @Builder.Default
    @JsonProperty("meetingType")
    @JsonAlias({"meeting_type", "meetingType", "type"})
    private MeetingType meetingType = MeetingType.INSTANT_ROOM;

    @JsonProperty("scheduledStartTime")
    @JsonAlias({"scheduled_start_time", "scheduledStartTime", "scheduled_at", "scheduledAt", "startTime", "start_time"})
    private LocalDateTime scheduledStartTime;

    @JsonProperty("scheduledEndTime")
    @JsonAlias({"scheduled_end_time", "scheduledEndTime", "endTime", "end_time"})
    private LocalDateTime scheduledEndTime;

    @JsonProperty("durationMinutes")
    @JsonAlias({"duration_minutes", "durationMinutes", "duration"})
    private Integer durationMinutes;

    private String timezone;

    @Builder.Default
    @JsonProperty("reminderMinutes")
    @JsonAlias({"reminder_minutes", "reminderMinutes"})
    private Integer reminderMinutes = 15;

    @Builder.Default
    @JsonProperty("recurrenceType")
    @JsonAlias({"recurrence_type", "recurrenceType"})
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    private String password;

    @JsonProperty("participantUserIds")
    @JsonAlias({"participant_user_ids", "participantUserIds", "invited_user_ids", "participantIds", "participant_ids"})
    private List<String> participantUserIds;

    @JsonProperty("participantEmails")
    @JsonAlias({"participant_emails", "participantEmails", "invited_emails", "invitedEmails"})
    private List<String> participantEmails;

    @Builder.Default
    @JsonProperty("allowParticipantChat")
    @JsonAlias({"allow_participant_chat", "allowParticipantChat", "allowChat", "allow_chat"})
    private boolean allowParticipantChat = true;

    @Builder.Default
    @JsonProperty("allowScreenSharing")
    @JsonAlias({"allow_screen_sharing", "allowScreenSharing", "allowScreenShare", "allow_screen_share"})
    private boolean allowScreenSharing = true;

    @Builder.Default
    @JsonProperty("muteParticipantsOnEntry")
    @JsonAlias({"mute_participants_on_entry", "muteParticipantsOnEntry", "muteOnEntry", "mute_on_entry"})
    private boolean muteParticipantsOnEntry = false;

    @Builder.Default
    @JsonProperty("allowParticipantVideo")
    @JsonAlias({"allow_participant_video", "allowParticipantVideo", "allowVideo", "allow_video"})
    private boolean allowParticipantVideo = true;

    @Builder.Default
    @JsonProperty("allowParticipantAudio")
    @JsonAlias({"allow_participant_audio", "allowParticipantAudio", "allowAudio", "allow_audio"})
    private boolean allowParticipantAudio = true;

    @Builder.Default
    @JsonProperty("isOpenRoom")
    @JsonAlias({"is_open_room", "isOpenRoom", "openRoom", "open_room"})
    private boolean isOpenRoom = false;

    // Backward compatibility fields
    @JsonProperty("scheduled_at")
    @JsonAlias({"scheduled_at", "scheduledAt"})
    private LocalDateTime scheduledAt;

    @JsonProperty("organization_id")
    @JsonAlias({"organization_id", "organizationId"})
    private String organizationId;

    public LocalDateTime getScheduledStartTime() {
        if (scheduledStartTime != null) {
            return scheduledStartTime;
        }
        return scheduledAt;
    }
}
