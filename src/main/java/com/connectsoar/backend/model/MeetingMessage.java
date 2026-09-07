package com.connectsoar.backend.model;

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
public class MeetingMessage {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("sender_user_id")
    private String senderUserId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("sender_email")
    private String senderEmail;

    private String message;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
