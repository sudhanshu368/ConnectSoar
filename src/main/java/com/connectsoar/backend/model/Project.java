package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Project {

    private String id;
    private String name;
    private String description;
    private ProjectStatus status = ProjectStatus.IN_PROGRESS;

    @JsonProperty("start_date")
    private LocalDate startDate;

    private LocalDate deadline;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_activity_at")
    private LocalDateTime lastActivityAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public Project() {
    }

    public Project(String id, String name, String description, ProjectStatus status, LocalDate startDate,
                   LocalDate deadline, String createdByUserId, LocalDateTime lastActivityAt,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status != null ? status : ProjectStatus.IN_PROGRESS;
        this.startDate = startDate != null ? startDate : LocalDate.now();
        this.deadline = deadline;
        this.createdByUserId = createdByUserId;
        this.lastActivityAt = lastActivityAt != null ? lastActivityAt : LocalDateTime.now();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private ProjectStatus status = ProjectStatus.IN_PROGRESS;
        private LocalDate startDate;
        private LocalDate deadline;
        private String createdByUserId;
        private LocalDateTime lastActivityAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(ProjectStatus status) { this.status = status; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder deadline(LocalDate deadline) { this.deadline = deadline; return this; }
        public Builder createdByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; return this; }
        public Builder lastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public Project build() {
            return new Project(id, name, description, status, startDate, deadline, createdByUserId, lastActivityAt, createdAt, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
