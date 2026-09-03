package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class UpdateMeetingRequest {

    private String title;
    private String description;

    @JsonProperty("scheduled_at")
    private LocalDateTime scheduledAt;

    private String status;

    public UpdateMeetingRequest() {
    }

    public UpdateMeetingRequest(String title, String description, LocalDateTime scheduledAt, String status) {
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String description;
        private LocalDateTime scheduledAt;
        private String status;

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder status(String status) { this.status = status; return this; }

        public UpdateMeetingRequest build() {
            return new UpdateMeetingRequest(title, description, scheduledAt, status);
        }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
