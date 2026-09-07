package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MeetingJoinResponse {

    @JsonProperty("meetingId")
    private String meetingId;

    @JsonProperty("meetingCode")
    private String meetingCode;

    @JsonProperty("meetingUrl")
    private String meetingUrl;

    @JsonProperty("roomId")
    private String roomId;

    private String role;
    private String status;

    private MeetingPermissionsDto permissions;

    @JsonProperty("sessionId")
    private String sessionId;

    @JsonProperty("meetingToken")
    private String meetingToken;

    @JsonProperty("expiresIn")
    private Long expiresIn;

    // Backward compatibility aliases for JSON serialization where needed
    @JsonProperty("meeting_id")
    public String getSnakeMeetingId() {
        return meetingId;
    }

    @JsonProperty("room_id")
    public String getSnakeRoomId() {
        return roomId;
    }

    @JsonProperty("meeting_token")
    public String getSnakeMeetingToken() {
        return meetingToken;
    }

    @JsonProperty("expires_in")
    public Long getSnakeExpiresIn() {
        return expiresIn;
    }
}
