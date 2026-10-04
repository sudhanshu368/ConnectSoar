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
public class RecordingStatusResponse {
    private String meetingId;

    @JsonProperty("isRecording")
    private boolean isRecording;

    @JsonProperty("startedAt")
    private LocalDateTime startedAt;

    @JsonProperty("recordingUrl")
    private String recordingUrl;

    private String message;
}
