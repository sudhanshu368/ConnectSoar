package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParticipantStatus {
    INVITED,
    ACCEPTED,
    DECLINED,
    JOINED,
    LEFT,
    REMOVED;

    @JsonCreator
    public static ParticipantStatus fromString(String val) {
        if (val == null || val.isBlank()) return INVITED;
        String normalized = val.trim().toUpperCase();
        for (ParticipantStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        return INVITED;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
