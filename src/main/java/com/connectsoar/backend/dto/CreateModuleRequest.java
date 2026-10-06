package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateModuleRequest {

    @NotBlank(message = "Module title is required")
    private String title;

    private String description;

    @JsonProperty("module_type")
    private String moduleType = "FULL_STACK"; // FRONTEND, BACKEND, FULL_STACK, GENERAL

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("assigned_to_name")
    private String assignedToName;

    private LocalDate deadline;

    public CreateModuleRequest() {
    }

    public CreateModuleRequest(String title, String description, String moduleType, String assignedToUserId, String assignedToName, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.moduleType = moduleType != null ? moduleType : "FULL_STACK";
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
