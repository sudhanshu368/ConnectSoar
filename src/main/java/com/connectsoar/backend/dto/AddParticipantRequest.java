package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AddParticipantRequest {

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    private String permission = "participant";

    public AddParticipantRequest() {
    }

    public AddParticipantRequest(String userId, String permission) {
        this.userId = userId;
        this.permission = permission != null ? permission : "participant";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String permission = "participant";

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder permission(String permission) { this.permission = permission; return this; }

        public AddParticipantRequest build() {
            return new AddParticipantRequest(userId, permission);
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}
