package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProjectActivityLogDto {

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

    public ProjectActivityLogDto() {
    }

    public ProjectActivityLogDto(String id, String projectId, String moduleId, String userId,
                                 String userName, String action, String oldStatus, String newStatus,
                                 String details, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.moduleId = moduleId;
        this.userId = userId;
        this.userName = userName;
        this.action = action;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.details = details;
        this.createdAt = createdAt;
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
