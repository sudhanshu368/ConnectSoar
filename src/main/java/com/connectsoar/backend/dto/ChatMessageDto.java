package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessageDto {
    private String id;

    @JsonProperty("meetingId")
    private String meetingId;

    @JsonProperty("senderUserId")
    private String senderUserId;

    @JsonProperty("senderName")
    private String senderName;

    @JsonProperty("senderEmail")
    private String senderEmail;

    private String message;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
}
