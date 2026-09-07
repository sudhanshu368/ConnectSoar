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
public class MeetingSession {

    private String id;

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("session_id")
    private String sessionId;

    @Builder.Default
    @JsonProperty("connection_status")
    private String connectionStatus = "CONNECTED";

    @Builder.Default
    @JsonProperty("microphone_enabled")
    private boolean microphoneEnabled = true;

    @Builder.Default
    @JsonProperty("camera_enabled")
    private boolean cameraEnabled = true;

    @Builder.Default
    @JsonProperty("screen_sharing")
    private boolean screenSharing = false;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("last_seen_at")
    private LocalDateTime lastSeenAt;

    @JsonProperty("left_at")
    private LocalDateTime leftAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
