package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectResponse {

    private String id;
    private String name;
    private String description;
    private String status;

    @JsonProperty("start_date")
    private LocalDate startDate;

    private LocalDate deadline;

    @JsonProperty("created_by_user_id")
    private String createdByUserId;

    @JsonProperty("last_activity_at")
    private LocalDateTime lastActivityAt;

    @JsonProperty("total_modules")
    private int totalModules;

    @JsonProperty("completed_modules")
    private int completedModules;

    @JsonProperty("overall_progress_percentage")
    private int overallProgressPercentage;

    @JsonProperty("frontend_progress_percentage")
    private int frontendProgressPercentage;

    @JsonProperty("backend_progress_percentage")
    private int backendProgressPercentage;

    @JsonProperty("is_delayed")
    private boolean isDelayed;

    @JsonProperty("is_hold_due_to_inactivity")
    private boolean isHoldDueToInactivity;

    @JsonProperty("days_inactive")
    private long daysInactive;

    private List<ProjectMemberDto> members = new ArrayList<>();
    private List<ModuleResponse> modules = new ArrayList<>();

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public ProjectResponse() {
    }

    public ProjectResponse(String id, String name, String description, String status, LocalDate startDate,
                           LocalDate deadline, String createdByUserId, LocalDateTime lastActivityAt,
                           int totalModules, int completedModules, int overallProgressPercentage,
                           int frontendProgressPercentage, int backendProgressPercentage,
                           boolean isDelayed, boolean isHoldDueToInactivity, long daysInactive,
                           List<ProjectMemberDto> members, List<ModuleResponse> modules,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startDate = startDate;
        this.deadline = deadline;
        this.createdByUserId = createdByUserId;
        this.lastActivityAt = lastActivityAt;
        this.totalModules = totalModules;
        this.completedModules = completedModules;
        this.overallProgressPercentage = overallProgressPercentage;
        this.frontendProgressPercentage = frontendProgressPercentage;
        this.backendProgressPercentage = backendProgressPercentage;
        this.isDelayed = isDelayed;
        this.isHoldDueToInactivity = isHoldDueToInactivity;
        this.daysInactive = daysInactive;
        this.members = members != null ? members : new ArrayList<>();
        this.modules = modules != null ? modules : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private String description;
        private String status;
        private LocalDate startDate;
        private LocalDate deadline;
        private String createdByUserId;
        private LocalDateTime lastActivityAt;
        private int totalModules;
        private int completedModules;
        private int overallProgressPercentage;
        private int frontendProgressPercentage;
        private int backendProgressPercentage;
        private boolean isDelayed;
        private boolean isHoldDueToInactivity;
        private long daysInactive;
        private List<ProjectMemberDto> members = new ArrayList<>();
        private List<ModuleResponse> modules = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder status(String status) { this.status = status; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder deadline(LocalDate deadline) { this.deadline = deadline; return this; }
        public Builder createdByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; return this; }
        public Builder lastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; return this; }
        public Builder totalModules(int totalModules) { this.totalModules = totalModules; return this; }
        public Builder completedModules(int completedModules) { this.completedModules = completedModules; return this; }
        public Builder overallProgressPercentage(int overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; return this; }
        public Builder frontendProgressPercentage(int frontendProgressPercentage) { this.frontendProgressPercentage = frontendProgressPercentage; return this; }
        public Builder backendProgressPercentage(int backendProgressPercentage) { this.backendProgressPercentage = backendProgressPercentage; return this; }
        public Builder isDelayed(boolean isDelayed) { this.isDelayed = isDelayed; return this; }
        public Builder isHoldDueToInactivity(boolean isHoldDueToInactivity) { this.isHoldDueToInactivity = isHoldDueToInactivity; return this; }
        public Builder daysInactive(long daysInactive) { this.daysInactive = daysInactive; return this; }
        public Builder members(List<ProjectMemberDto> members) { this.members = members; return this; }
        public Builder modules(List<ModuleResponse> modules) { this.modules = modules; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public ProjectResponse build() {
            return new ProjectResponse(id, name, description, status, startDate, deadline, createdByUserId,
                    lastActivityAt, totalModules, completedModules, overallProgressPercentage,
                    frontendProgressPercentage, backendProgressPercentage, isDelayed, isHoldDueToInactivity,
                    daysInactive, members, modules, createdAt, updatedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public String getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(String createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public int getTotalModules() { return totalModules; }
    public void setTotalModules(int totalModules) { this.totalModules = totalModules; }

    public int getCompletedModules() { return completedModules; }
    public void setCompletedModules(int completedModules) { this.completedModules = completedModules; }

    public int getOverallProgressPercentage() { return overallProgressPercentage; }
    public void setOverallProgressPercentage(int overallProgressPercentage) { this.overallProgressPercentage = overallProgressPercentage; }

    public int getFrontendProgressPercentage() { return frontendProgressPercentage; }
    public void setFrontendProgressPercentage(int frontendProgressPercentage) { this.frontendProgressPercentage = frontendProgressPercentage; }

    public int getBackendProgressPercentage() { return backendProgressPercentage; }
    public void setBackendProgressPercentage(int backendProgressPercentage) { this.backendProgressPercentage = backendProgressPercentage; }

    public boolean isDelayed() { return isDelayed; }
    public void setDelayed(boolean delayed) { isDelayed = delayed; }

    public boolean isHoldDueToInactivity() { return isHoldDueToInactivity; }
    public void setHoldDueToInactivity(boolean holdDueToInactivity) { isHoldDueToInactivity = holdDueToInactivity; }

    public long getDaysInactive() { return daysInactive; }
    public void setDaysInactive(long daysInactive) { this.daysInactive = daysInactive; }

    public List<ProjectMemberDto> getMembers() { return members; }
    public void setMembers(List<ProjectMemberDto> members) { this.members = members; }

    public List<ModuleResponse> getModules() { return modules; }
    public void setModules(List<ModuleResponse> modules) { this.modules = modules; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
