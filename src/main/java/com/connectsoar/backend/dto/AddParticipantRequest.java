package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddParticipantRequest {

    @JsonProperty("userId")
    private String userId;

    private String email;

    @Builder.Default
    private String permission = "participant";

    @JsonProperty("user_id")
    public void setSnakeUserId(String snakeUserId) {
        if (this.userId == null) {
            this.userId = snakeUserId;
        }
    }
}
