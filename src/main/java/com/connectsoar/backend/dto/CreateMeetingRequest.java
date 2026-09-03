package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CreateMeetingRequest {

    @NotBlank(message = "Meeting title is required")
    private String title;

    private String description;

    @JsonProperty("scheduled_at")
    private LocalDateTime scheduledAt;

    @JsonProperty("organization_id")
    private String organizationId;

    public CreateMeetingRequest() {
    }

    public CreateMeetingRequest(String title, String description, LocalDateTime scheduledAt, String organizationId) {
        this.title = title;
        this.description = description;
        this.scheduledAt = scheduledAt;
        this.organizationId = organizationId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String title;
        private String description;
        private LocalDateTime scheduledAt;
        private String organizationId;

        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder scheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; return this; }
        public Builder organizationId(String organizationId) { this.organizationId = organizationId; return this; }

        public CreateMeetingRequest build() {
            return new CreateMeetingRequest(title, description, scheduledAt, organizationId);
        }
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
}
