package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AvatarUploadResponse {

    @JsonProperty("path")
    private String path;

    @JsonProperty("upload_url")
    private String uploadUrl;

    @JsonProperty("signed_url")
    private String signedUrl;

    @JsonProperty("expires_in")
    private long expiresIn;

    public AvatarUploadResponse() {
    }

    public AvatarUploadResponse(String path, String uploadUrl, String signedUrl, long expiresIn) {
        this.path = path;
        this.uploadUrl = uploadUrl;
        this.signedUrl = signedUrl;
        this.expiresIn = expiresIn;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String path;
        private String uploadUrl;
        private String signedUrl;
        private long expiresIn;

        public Builder path(String path) { this.path = path; return this; }
        public Builder uploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; return this; }
        public Builder signedUrl(String signedUrl) { this.signedUrl = signedUrl; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }

        public AvatarUploadResponse build() {
            return new AvatarUploadResponse(path, uploadUrl, signedUrl, expiresIn);
        }
    }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getUploadUrl() { return uploadUrl; }
    public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }

    public String getSignedUrl() { return signedUrl; }
    public void setSignedUrl(String signedUrl) { this.signedUrl = signedUrl; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
