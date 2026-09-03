package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AvatarUploadRequest {

    @NotBlank(message = "File name is required")
    @JsonProperty("file_name")
    private String fileName;

    @NotBlank(message = "Content type is required (e.g. image/jpeg, image/png, image/webp)")
    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("file_size_bytes")
    private long fileSizeBytes;

    public AvatarUploadRequest() {
    }

    public AvatarUploadRequest(String fileName, String contentType, long fileSizeBytes) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.fileSizeBytes = fileSizeBytes;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileName;
        private String contentType;
        private long fileSizeBytes;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder fileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }

        public AvatarUploadRequest build() {
            return new AvatarUploadRequest(fileName, contentType, fileSizeBytes);
        }
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
}
