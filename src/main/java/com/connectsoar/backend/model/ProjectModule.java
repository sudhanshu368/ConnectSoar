package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.ModuleStatus;
import com.connectsoar.backend.enums.ModuleType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectModule {

    private String id;

    @JsonProperty("project_id")
    private String projectId;

    private String title;
    private String description;

    @JsonProperty("module_type")
    private ModuleType moduleType = ModuleType.FULL_STACK;

    private ModuleStatus status = ModuleStatus.TODO;

    @JsonProperty("progress_percentage")
    private int progressPercentage = 0;

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("assigned_to_name")
    private String assignedToName;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    private LocalDate deadline;

    @JsonProperty("last_updated_by_user_id")
    private String lastUpdatedByUserId;

    @JsonProperty("last_updated_by_name")
    private String lastUpdatedByName;

    @JsonProperty("last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public ProjectModule() {
    }

    public ProjectModule(String id, String projectId, String title, String description, ModuleType moduleType,
                         ModuleStatus status, int progressPercentage, String assignedToUserId, String assignedToName,
                         String createdByUserId, LocalDate deadline, String lastUpdatedByUserId,
                         String lastUpdatedByName, LocalDateTime lastUpdatedAt, LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.moduleType = moduleType != null ? moduleType : ModuleType.FULL_STACK;
        this.status = status != null ? status : ModuleStatus.TODO;
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
        this.assignedToUserId = assignedToUserId;
        this.assignedToName = assignedToName;
        this.createdByUserId = createdByUserId;
        this.deadline = deadline;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.lastUpdatedByName = lastUpdatedByName;
        this.lastUpdatedAt = lastUpdatedAt != null ? lastUpdatedAt : LocalDateTime.now();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectId;
        private String title;
        private String description;
        private ModuleType moduleType = ModuleType.FULL_STACK;
        private ModuleStatus status = ModuleStatus.TODO;
        private int progressPercentage = 0;
        private String assignedToUserId;
        private String assignedToName;
        private String createdByUserId;
        private LocalDate deadline;
        private String lastUpdatedByUserId;
        private String lastUpdatedByName;
        private LocalDateTime lastUpdatedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder moduleType(ModuleType moduleType) { this.moduleType = moduleType; return this; }
        public Builder status(ModuleStatus status) { this.status = status; return this; }
        public Builder progressPercentage(int progressPercentage) { this.progressPercentage = progressPercentage; return this; }
        public Builder assignedToUserId(String assignedToUserId) { this.assignedToUserId = assignedToUserId; return this; }
        public Builder assignedToName(String assignedToName) { this.assignedToName = assignedToName; return this; }
        public Builder createdByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; return this; }
        public Builder deadline(LocalDate deadline) { this.deadline = deadline; return this; }
        public Builder lastUpdatedByUserId(String lastUpdatedByUserId) { this.lastUpdatedByUserId = lastUpdatedByUserId; return this; }
        public Builder lastUpdatedByName(String lastUpdatedByName) { this.lastUpdatedByName = lastUpdatedByName; return this; }
        public Builder lastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProjectModule build() {
            return new ProjectModule(id, projectId, title, description, moduleType, status, progressPercentage,
                    assignedToUserId, assignedToName, createdByUserId, deadline, lastUpdatedByUserId,
                    lastUpdatedByName, lastUpdatedAt, createdAt, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ModuleType getModuleType() { return moduleType; }
    public void setModuleType(ModuleType moduleType) { this.moduleType = moduleType; }

    public ModuleStatus getStatus() { return status; }
    public void setStatus(ModuleStatus status) { this.status = status; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) { this.progressPercentage = Math.max(0, Math.min(100, progressPercentage)); }

    public String getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(String assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getLastUpdatedByUserId() { return lastUpdatedByUserId; }
    public void setLastUpdatedByUserId(String lastUpdatedByUserId) { this.lastUpdatedByUserId = lastUpdatedByUserId; }

    public String getLastUpdatedByName() { return lastUpdatedByName; }
    public void setLastUpdatedByName(String lastUpdatedByName) { this.lastUpdatedByName = lastUpdatedByName; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
