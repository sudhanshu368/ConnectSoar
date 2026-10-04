package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LobbyRequestDto {
    private String userId;
    private String name;
    private String email;
    private String imageUrl;
    private String status; // PENDING, ADMITTED, DENIED

    @JsonProperty("requestedAt")
    private LocalDateTime requestedAt;
}
