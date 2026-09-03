package com.connectsoar.backend.dto;

public class CreateEmployeeResponse {
    private String status;
    private String message;
    private String userId;
    private String email;
    private String role;

    public CreateEmployeeResponse() {}

    public CreateEmployeeResponse(String status, String message, String userId, String email, String role) {
        this.status = status;
        this.message = message;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;
        private String message;
        private String userId;
        private String email;
        private String role;

        public Builder status(String status) { this.status = status; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public CreateEmployeeResponse build() {
            return new CreateEmployeeResponse(status, message, userId, email, role);
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
