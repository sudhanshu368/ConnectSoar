package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("access_token")
    private String accessToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String refreshToken;
        private String accessToken;

        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }

        public RefreshTokenRequest build() {
            return new RefreshTokenRequest(refreshToken, accessToken);
        }
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
