package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordChangeRequiredData {

    @JsonProperty("reset_password")
    private boolean resetPassword = true;

    @JsonProperty("password_reset_token")
    private String passwordResetToken;

    private UserSummary user;

    public PasswordChangeRequiredData() {
    }

    public PasswordChangeRequiredData(boolean resetPassword, String passwordResetToken, UserSummary user) {
        this.resetPassword = resetPassword;
        this.passwordResetToken = passwordResetToken;
        this.user = user;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean resetPassword = true;
        private String passwordResetToken;
        private UserSummary user;

        public Builder resetPassword(boolean resetPassword) { this.resetPassword = resetPassword; return this; }
        public Builder passwordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; return this; }
        public Builder user(UserSummary user) { this.user = user; return this; }

        public PasswordChangeRequiredData build() {
            return new PasswordChangeRequiredData(resetPassword, passwordResetToken, user);
        }
    }

    public boolean isResetPassword() { return resetPassword; }
    public void setResetPassword(boolean resetPassword) { this.resetPassword = resetPassword; }

    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }

    public UserSummary getUser() { return user; }
    public void setUser(UserSummary user) { this.user = user; }

    public static class UserSummary {
        private String id;
        private String email;
        private String name;
        private String role;

        public UserSummary() {}

        public UserSummary(String id, String email, String name, String role) {
            this.id = id;
            this.email = email;
            this.name = name;
            this.role = role;
        }

        public static UserSummaryBuilder builder() {
            return new UserSummaryBuilder();
        }

        public static class UserSummaryBuilder {
            private String id;
            private String email;
            private String name;
            private String role;

            public UserSummaryBuilder id(String id) { this.id = id; return this; }
            public UserSummaryBuilder email(String email) { this.email = email; return this; }
            public UserSummaryBuilder name(String name) { this.name = name; return this; }
            public UserSummaryBuilder role(String role) { this.role = role; return this; }

            public UserSummary build() {
                return new UserSummary(id, email, name, role);
            }
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}
