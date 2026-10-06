package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectSummaryStatsDto {

    @JsonProperty("total_projects")
    private int totalProjects;

    @JsonProperty("active_projects")
    private int activeProjects;

    @JsonProperty("hold_projects")
    private int holdProjects;

    @JsonProperty("completed_projects")
    private int completedProjects;

    @JsonProperty("delayed_projects")
    private int delayedProjects;

    @JsonProperty("total_modules")
    private int totalModules;

    @JsonProperty("completed_modules")
    private int completedModules;

    @JsonProperty("delayed_modules")
    private int delayedModules;

    public ProjectSummaryStatsDto() {
    }

    public ProjectSummaryStatsDto(int totalProjects, int activeProjects, int holdProjects,
                                 int completedProjects, int delayedProjects, int totalModules,
                                 int completedModules, int delayedModules) {
        this.totalProjects = totalProjects;
        this.activeProjects = activeProjects;
        this.holdProjects = holdProjects;
        this.completedProjects = completedProjects;
        this.delayedProjects = delayedProjects;
        this.totalModules = totalModules;
        this.completedModules = completedModules;
        this.delayedModules = delayedModules;
    }

    public int getTotalProjects() { return totalProjects; }
    public void setTotalProjects(int totalProjects) { this.totalProjects = totalProjects; }

    public int getActiveProjects() { return activeProjects; }
    public void setActiveProjects(int activeProjects) { this.activeProjects = activeProjects; }

    public int getHoldProjects() { return holdProjects; }
    public void setHoldProjects(int holdProjects) { this.holdProjects = holdProjects; }

    public int getCompletedProjects() { return completedProjects; }
    public void setCompletedProjects(int completedProjects) { this.completedProjects = completedProjects; }

    public int getDelayedProjects() { return delayedProjects; }
    public void setDelayedProjects(int delayedProjects) { this.delayedProjects = delayedProjects; }

    public int getTotalModules() { return totalModules; }
    public void setTotalModules(int totalModules) { this.totalModules = totalModules; }

    public int getCompletedModules() { return completedModules; }
    public void setCompletedModules(int completedModules) { this.completedModules = completedModules; }

    public int getDelayedModules() { return delayedModules; }
    public void setDelayedModules(int delayedModules) { this.delayedModules = delayedModules; }
}
