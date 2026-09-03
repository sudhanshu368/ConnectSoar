package com.connectsoar.backend.dto;

public class AuthTokenResponse {
    private String accessToken;
    private String previousAccessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String email;
    private String role;

    public AuthTokenResponse() {}

    public AuthTokenResponse(String accessToken, String previousAccessToken, String refreshToken, String tokenType, Long expiresIn, String email, String role) {
        this.accessToken = accessToken;
        this.previousAccessToken = previousAccessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.email = email;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String accessToken;
        private String previousAccessToken;
        private String refreshToken;
        private String tokenType;
        private Long expiresIn;
        private String email;
        private String role;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder previousAccessToken(String previousAccessToken) { this.previousAccessToken = previousAccessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder tokenType(String tokenType) { this.tokenType = tokenType; return this; }
        public Builder expiresIn(Long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public AuthTokenResponse build() {
            return new AuthTokenResponse(accessToken, previousAccessToken, refreshToken, tokenType, expiresIn, email, role);
        }
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getPreviousAccessToken() { return previousAccessToken; }
    public void setPreviousAccessToken(String previousAccessToken) { this.previousAccessToken = previousAccessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
