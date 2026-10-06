package com.connectsoar.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProjectActivityLog {

    private String id;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("module_id")
    private String moduleId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("user_name")
    private String userName;

    private String action;

    @JsonProperty("old_status")
    private String oldStatus;

    @JsonProperty("new_status")
    private String newStatus;

    private String details;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public ProjectActivityLog() {
    }

    public ProjectActivityLog(String id, String projectId, String moduleId, String userId, String userName,
                              String action, String oldStatus, String newStatus, String details, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.moduleId = moduleId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.details = details;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectId;
        private String moduleId;
        private String userId;
        private String userName;
        private String action;
        private String oldStatus;
        private String newStatus;
        private String details;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder moduleId(String moduleId) { this.moduleId = moduleId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder oldStatus(String oldStatus) { this.oldStatus = oldStatus; return this; }
        public Builder newStatus(String newStatus) { this.newStatus = newStatus; return this; }
        public Builder details(String details) { this.details = details; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ProjectActivityLog build() {
            return new ProjectActivityLog(id, projectId, moduleId, userId, userName, action, oldStatus, newStatus, details, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getModuleId() { return moduleId; }
    public void setModuleId(String moduleId) { this.moduleId = moduleId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
