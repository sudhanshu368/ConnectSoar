package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.MeetingPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MeetingParticipant {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("user_id")
    private String userId;

    private MeetingPermission permission = MeetingPermission.participant;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public MeetingParticipant() {
    }

    public MeetingParticipant(String id, String meetingId, String userId, MeetingPermission permission,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.userId = userId;
        this.permission = permission != null ? permission : MeetingPermission.participant;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String meetingId;
        private String userId;
        private MeetingPermission permission = MeetingPermission.participant;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder meetingId(String meetingId) { this.meetingId = meetingId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder permission(MeetingPermission permission) { this.permission = permission; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public MeetingParticipant build() {
            return new MeetingParticipant(id, meetingId, userId, permission, createdAt, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public MeetingPermission getPermission() { return permission; }
    public void setPermission(MeetingPermission permission) { this.permission = permission; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
