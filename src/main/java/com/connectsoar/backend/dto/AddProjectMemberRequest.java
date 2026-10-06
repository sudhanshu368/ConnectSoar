package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class AddProjectMemberRequest {

    @NotBlank(message = "User ID is required")
    @JsonProperty("user_id")
    private String userId;

    @NotBlank(message = "Member role is required")
    @JsonProperty("member_role")
    private String memberRole; // MANAGER, FRONTEND, BACKEND, FULL_STACK, MEMBER

    public AddProjectMemberRequest() {
    }

    public AddProjectMemberRequest(String userId, String memberRole) {
        this.userId = userId;
        this.memberRole = memberRole;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMemberRole() { return memberRole; }
    public void setMemberRole(String memberRole) { this.memberRole = memberRole; }
}
