package com.connectsoar.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateParticipantRequest {

    @NotBlank(message = "Permission is required (host, co_host, participant)")
    private String permission;

    public UpdateParticipantRequest() {
    }

    public UpdateParticipantRequest(String permission) {
        this.permission = permission;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String permission;

        public Builder permission(String permission) { this.permission = permission; return this; }

        public UpdateParticipantRequest build() {
            return new UpdateParticipantRequest(permission);
        }
    }

    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
}
