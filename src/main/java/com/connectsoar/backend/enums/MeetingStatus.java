package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MeetingStatus {
    SCHEDULED,
    LIVE,
    COMPLETED,
    CANCELLED;

    public static final MeetingStatus scheduled = SCHEDULED;
    public static final MeetingStatus ongoing = LIVE;
    public static final MeetingStatus completed = COMPLETED;
    public static final MeetingStatus cancelled = CANCELLED;

    @JsonCreator
    public static MeetingStatus fromString(String val) {
        if (val == null || val.isBlank()) return SCHEDULED;
        String normalized = val.trim().toUpperCase();
        if ("ONGOING".equals(normalized)) {
            return LIVE;
        }
        for (MeetingStatus s : values()) {
            if (s.name().equalsIgnoreCase(normalized)) {
                return s;
            }
        }
        return SCHEDULED;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
