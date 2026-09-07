package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinCodeRequest {

    @NotBlank(message = "Meeting code is required")
    @JsonProperty("meetingCode")
    private String meetingCode;

    private String password;
}
