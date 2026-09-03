package com.connectsoar.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateRoleRequest {

    @NotBlank(message = "Role is required (admin, employee)")
    private String role;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(String role) {
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String role;

        public Builder role(String role) { this.role = role; return this; }

        public UpdateRoleRequest build() {
            return new UpdateRoleRequest(role);
        }
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
