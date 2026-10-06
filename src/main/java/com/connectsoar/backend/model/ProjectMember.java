package com.connectsoar.backend.model;

import com.connectsoar.backend.enums.ProjectMemberRole;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProjectMember {

    private String id;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("member_role")
    private ProjectMemberRole memberRole = ProjectMemberRole.FULL_STACK;

    @JsonProperty("assigned_at")
    private LocalDateTime assignedAt;

    public ProjectMember() {
    }

    public ProjectMember(String id, String projectId, String userId, ProjectMemberRole memberRole, LocalDateTime assignedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.memberRole = memberRole != null ? memberRole : ProjectMemberRole.FULL_STACK;
        this.assignedAt = assignedAt != null ? assignedAt : LocalDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectId;
        private String userId;
        private ProjectMemberRole memberRole = ProjectMemberRole.FULL_STACK;
        private LocalDateTime assignedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder memberRole(ProjectMemberRole memberRole) { this.memberRole = memberRole; return this; }
        public Builder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }

        public ProjectMember build() {
            return new ProjectMember(id, projectId, userId, memberRole, assignedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public ProjectMemberRole getMemberRole() { return memberRole; }
    public void setMemberRole(ProjectMemberRole memberRole) { this.memberRole = memberRole; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
