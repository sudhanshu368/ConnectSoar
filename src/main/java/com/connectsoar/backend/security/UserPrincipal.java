package com.connectsoar.backend.security;

import com.connectsoar.backend.enums.Role;
import com.connectsoar.backend.enums.UserStatus;

public class UserPrincipal {

    private String userId;
    private String email;
    private String name;
    private Role role = Role.employee;
    private UserStatus status = UserStatus.active;
    private boolean resetPassword = false;
    private String tokenType = "access";

    public UserPrincipal() {
    }

    public UserPrincipal(String userId, String email, String name, Role role, UserStatus status, boolean resetPassword, String tokenType) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role != null ? role : Role.employee;
        this.status = status != null ? status : UserStatus.active;
        this.resetPassword = resetPassword;
        this.tokenType = tokenType != null ? tokenType : "access";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userId;
        private String email;
        private String name;
        private Role role = Role.employee;
        private UserStatus status = UserStatus.active;
        private boolean resetPassword = false;
        private String tokenType = "access";

        public Builder userId(String userId) { this.userId = userId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder status(UserStatus status) { this.status = status; return this; }
        public Builder resetPassword(boolean resetPassword) { this.resetPassword = resetPassword; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }

        public UserPrincipal build() {
            return new UserPrincipal(userId, email, name, role, status, resetPassword, tokenType);
        }
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public boolean isResetPassword() { return resetPassword; }
    public void setResetPassword(boolean resetPassword) { this.resetPassword = resetPassword; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public boolean isAdmin() {
        return Role.admin.equals(this.role);
    }

    public boolean isEmployee() {
        return Role.employee.equals(this.role);
    }

    public boolean isActive() {
        return UserStatus.active.equals(this.status);
    }

    public boolean isSuspended() {
        return UserStatus.suspended.equals(this.status);
    }

    public boolean isInactive() {
        return UserStatus.inactive.equals(this.status);
    }
}
