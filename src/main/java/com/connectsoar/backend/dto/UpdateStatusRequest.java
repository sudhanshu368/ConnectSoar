package com.connectsoar.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateStatusRequest {

    @NotBlank(message = "Status is required (active, inactive, suspended)")
    private String status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String status;

        public Builder status(String status) { this.status = status; return this; }

        public UpdateStatusRequest build() {
            return new UpdateStatusRequest(status);
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
