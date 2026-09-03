package com.connectsoar.backend.enums;

public enum MeetingPermission {
    host,
    co_host,
    participant;

    public static MeetingPermission fromString(String val) {
        if (val == null) return participant;
        for (MeetingPermission p : values()) {
            if (p.name().equalsIgnoreCase(val)) {
                return p;
            }
        }
        return participant;
    }
}
