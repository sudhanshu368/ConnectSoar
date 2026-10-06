package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class UpdateModuleStatusRequest {

    @NotBlank(message = "Status is required")
    private String status; // TODO, IN_PROGRESS, REVIEW, COMPLETED, BLOCKED

    @JsonProperty("progress_percentage")
    private Integer progressPercentage; // 0 - 100

    private String comment;

    public UpdateModuleStatusRequest() {
    }

    public UpdateModuleStatusRequest(String status, Integer progressPercentage, String comment) {
        this.status = status;
        this.progressPercentage = progressPercentage;
        this.comment = comment;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(Integer progressPercentage) { this.progressPercentage = progressPercentage; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
