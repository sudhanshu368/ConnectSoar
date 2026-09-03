package com.connectsoar.backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class OrganizationMember {

    private String id;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("user_id")
    private String userId;

    private String role;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public OrganizationMember() {
    }

    public OrganizationMember(String id, String organizationId, String userId, String role, LocalDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String organizationId;
        private String userId;
        private String role;
        private LocalDateTime createdAt;

        public Builder id(String id) { this.id = id; return this; }
        public Builder organizationId(String organizationId) { this.organizationId = organizationId; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public OrganizationMember build() {
            return new OrganizationMember(id, organizationId, userId, role, createdAt);
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
