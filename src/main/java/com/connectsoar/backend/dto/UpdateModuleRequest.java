package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class UpdateModuleRequest {

    private String title;
    private String description;

    @JsonProperty("module_type")
    private String moduleType;

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("assigned_to_name")
    private String assignedToName;

    private LocalDate deadline;

    public UpdateModuleRequest() {
    }

    public UpdateModuleRequest(String title, String description, String moduleType, String assignedToUserId, String assignedToName, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.moduleType = moduleType;
        this.assignedToUserId = assignedToUserId;
        this.assignedToName = assignedToName;
        this.deadline = deadline;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getModuleType() { return moduleType; }
    public void setModuleType(String moduleType) { this.moduleType = moduleType; }

    public String getAssignedToUserId() { return assignedToUserId; }
    public void setAssignedToUserId(String assignedToUserId) { this.assignedToUserId = assignedToUserId; }

    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
