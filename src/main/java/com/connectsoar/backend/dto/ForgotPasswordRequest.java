package com.connectsoar.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    public ForgotPasswordRequest() {
    }

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String email;

        public Builder email(String email) { this.email = email; return this; }

        public ForgotPasswordRequest build() {
            return new ForgotPasswordRequest(email);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
