package com.connectsoar.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeetingJoinResponse {

    @JsonProperty("meeting_id")
    private String meetingId;

    @JsonProperty("room_id")
    private String roomId;

    @JsonProperty("meeting_token")
    private String meetingToken;

    @JsonProperty("expires_in")
    private long expiresIn;

    private String role;

    public MeetingJoinResponse() {
    }

    public MeetingJoinResponse(String meetingId, String roomId, String meetingToken, long expiresIn, String role) {
        this.meetingId = meetingId;
        this.roomId = roomId;
        this.meetingToken = meetingToken;
        this.expiresIn = expiresIn;
        this.role = role;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String meetingId;
        private String roomId;
        private String meetingToken;
        private long expiresIn;
        private String role;

        public Builder meetingId(String meetingId) { this.meetingId = meetingId; return this; }
        public Builder roomId(String roomId) { this.roomId = roomId; return this; }
        public Builder meetingToken(String meetingToken) { this.meetingToken = meetingToken; return this; }
        public Builder expiresIn(long expiresIn) { this.expiresIn = expiresIn; return this; }
        public Builder role(String role) { this.role = role; return this; }

        public MeetingJoinResponse build() {
            return new MeetingJoinResponse(meetingId, roomId, meetingToken, expiresIn, role);
        }
    }

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getMeetingToken() { return meetingToken; }
    public void setMeetingToken(String meetingToken) { this.meetingToken = meetingToken; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
