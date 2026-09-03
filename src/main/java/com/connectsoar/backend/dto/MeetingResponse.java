package com.connectsoar.backend.dto;

import com.connectsoar.backend.enums.MeetingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MeetingResponse {

    private String id;

    @JsonProperty("organization_id")
    private String organizationId;

    private String title;
    private String description;

    @JsonProperty("host_id")
    private String hostId;

    @JsonProperty("host_name")
    private String hostName;

    private MeetingStatus status;

    @JsonProperty("scheduled_at")
    private LocalDateTime scheduledAt;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    @JsonProperty("ended_at")
    private LocalDateTime endedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @JsonProperty("participants_count")
    private int participantsCount;

    public MeetingResponse() {
    }

    public MeetingResponse(String id, String organizationId, String title, String description, String hostId,
                           String hostName, MeetingStatus status, LocalDateTime scheduledAt, LocalDateTime startedAt,
                           LocalDateTime endedAt, LocalDateTime createdAt, LocalDateTime updatedAt, int participantsCount) {
        this.id = id;
        this.organizationId = organizationId;
        this.title = title;
        this.description = description;
        this.hostId = hostId;
        this.hostName = hostName;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.participantsCount = participantsCount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String organizationId;
        private String title;
        private String description;
        private String hostId;
        private String hostName;
        private MeetingStatus status;
        private LocalDateTime scheduledAt;
        private LocalDateTime startedAt;
        private LocalDateTime endedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private int participantsCount;

        public Builder id(String id) { this.id = id; return this; }
        public Builder organizationId(String organizationId) { this.organizationId = organizationId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder hostId(String hostId) { this.hostId = hostId; return this; }
        public Builder hostName(String hostName) { this.hostName = hostName; return this; }
        public Builder status(MeetingStatus status) { this.status = status; return this; }
        public Builder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder endedAt(LocalDateTime endedAt) { this.endedAt = endedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder participantsCount(int participantsCount) { this.participantsCount = participantsCount; return this; }

        public MeetingResponse build() {
            return new MeetingResponse(id, organizationId, title, description, hostId, hostName, status, scheduledAt, startedAt, endedAt, createdAt, updatedAt, participantsCount);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public MeetingStatus getStatus() { return status; }
    public void setStatus(MeetingStatus status) { this.status = status; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getParticipantsCount() { return participantsCount; }
    public void setParticipantsCount(int participantsCount) { this.participantsCount = participantsCount; }
}
