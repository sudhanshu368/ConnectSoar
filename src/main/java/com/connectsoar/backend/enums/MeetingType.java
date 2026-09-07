package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MeetingType {
    INSTANT_ROOM,
    SCHEDULED_MEETING;

    @JsonCreator
    public static MeetingType fromString(String val) {
        if (val == null || val.isBlank()) return INSTANT_ROOM;
        String normalized = val.trim().toUpperCase();
        for (MeetingType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return INSTANT_ROOM;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
