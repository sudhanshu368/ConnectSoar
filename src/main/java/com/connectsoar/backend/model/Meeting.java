package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.MeetingStatus;
import com.connectsoar.backend.enums.MeetingType;
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
public class Meeting {

    private String id;

    @JsonProperty("meeting_code")
    private String meetingCode;

    @JsonProperty("meeting_url")
    private String meetingUrl;

    private String title;
    private String description;

    @Builder.Default
    @JsonProperty("meeting_type")
    private MeetingType meetingType = MeetingType.INSTANT_ROOM;

    @JsonProperty("host_user_id")
    private String hostUserId;

    @JsonProperty("password_hash")
    private String passwordHash;

    @Builder.Default
    private MeetingStatus status = MeetingStatus.SCHEDULED;

    @JsonProperty("scheduled_start_time")
    private LocalDateTime scheduledStartTime;

    @JsonProperty("scheduled_end_time")
    private LocalDateTime scheduledEndTime;

    @JsonProperty("duration_minutes")
    private Integer durationMinutes;

    private String timezone;

    @JsonProperty("reminder_minutes")
    private Integer reminderMinutes;

    @Builder.Default
    @JsonProperty("recurrence_type")
    private RecurrenceType recurrenceType = RecurrenceType.NONE;

    @Builder.Default
    @JsonProperty("allow_participant_chat")
    private boolean allowParticipantChat = true;

    @Builder.Default
    @JsonProperty("allow_screen_sharing")
    private boolean allowScreenSharing = true;

    @Builder.Default
    @JsonProperty("mute_participants_on_entry")
    private boolean muteParticipantsOnEntry = false;

    @Builder.Default
    @JsonProperty("allow_participant_video")
    private boolean allowParticipantVideo = true;

    @Builder.Default
    @JsonProperty("allow_participant_audio")
    private boolean allowParticipantAudio = true;

    @Builder.Default
    @JsonProperty("is_open_room")
    private boolean isOpenRoom = false;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @JsonProperty("ended_at")
    private LocalDateTime endedAt;

    public boolean isOpenRoom() {
        return isOpenRoom;
    }

    public void setOpenRoom(boolean openRoom) {
        this.isOpenRoom = openRoom;
    }

    public void setIsOpenRoom(boolean isOpenRoom) {
        this.isOpenRoom = isOpenRoom;
    }

    public boolean getIsOpenRoom() {
        return isOpenRoom;
    }

    // Backward compatibility helper methods
    public String getHostId() {
        return hostUserId;
    }

    public void setHostId(String hostId) {
        this.hostUserId = hostId;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledStartTime;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledStartTime = scheduledAt;
    }

    public static class MeetingBuilder {
        public MeetingBuilder hostId(String hostId) {
            return this.hostUserId(hostId);
        }

        public MeetingBuilder scheduledAt(LocalDateTime scheduledAt) {
            return this.scheduledStartTime(scheduledAt);
        }
    }
}
