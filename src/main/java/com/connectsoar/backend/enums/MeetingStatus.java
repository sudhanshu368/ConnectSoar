package com.connectsoar.backend.enums;

public enum MeetingStatus {
    scheduled,
    ongoing,
    completed,
    cancelled;

    public static MeetingStatus fromString(String val) {
        if (val == null) return scheduled;
        for (MeetingStatus s : values()) {
            if (s.name().equalsIgnoreCase(val)) {
                return s;
            }
        }
        return scheduled;
    }
}
