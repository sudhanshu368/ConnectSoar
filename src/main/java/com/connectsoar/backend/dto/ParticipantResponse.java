package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingPermission;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ParticipantResponse {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("user_id")
    private String userId;

    private String name;
    private String email;
    private MeetingPermission permission;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public ParticipantResponse() {
    }

    public ParticipantResponse(String id, String meetingId, String userId, String name, String email,
                               MeetingPermission permission, LocalDateTime createdAt) {
        this.id = id;
        this.meetingId = meetingId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.permission = permission;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String meetingId;
        private String userId;
        private String name;
        private String email;
        private MeetingPermission permission;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder meetingId(String meetingId) { this.meetingId = meetingId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder permission(MeetingPermission permission) { this.permission = permission; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ParticipantResponse build() {
            return new ParticipantResponse(id, meetingId, userId, name, email, permission, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public MeetingPermission getPermission() { return permission; }
    public void setPermission(MeetingPermission permission) { this.permission = permission; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
