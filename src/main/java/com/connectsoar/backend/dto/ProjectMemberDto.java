package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProjectMemberDto {

    private String id;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("user_id")
    private String userId;

    private String name;
    private String email;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("member_role")
    private String memberRole;

    @JsonProperty("assigned_at")
    private LocalDateTime assignedAt;

    public ProjectMemberDto() {
    }

    public ProjectMemberDto(String id, String projectId, String userId, String name, String email,
                            String imageUrl, String memberRole, LocalDateTime assignedAt) {
        this.id = id;
        this.projectId = projectId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.imageUrl = imageUrl;
        this.memberRole = memberRole;
        this.assignedAt = assignedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String projectId;
        private String userId;
        private String name;
        private String email;
        private String imageUrl;
        private String memberRole;
        private LocalDateTime assignedAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder projectId(String projectId) { this.projectId = projectId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public Builder memberRole(String memberRole) { this.memberRole = memberRole; return this; }
        public Builder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }

        public ProjectMemberDto build() {
            return new ProjectMemberDto(id, projectId, userId, name, email, imageUrl, memberRole, assignedAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
