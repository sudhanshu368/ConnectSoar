package com.connectsoar.backend.enums;

public enum ProjectStatus {
    PLANNING,
    IN_PROGRESS,
    ON_HOLD,
    COMPLETED,
    CANCELLED;

    public static ProjectStatus fromString(String val) {
        if (val == null) return IN_PROGRESS;
        for (ProjectStatus s : values()) {
            if (s.name().equalsIgnoreCase(val)) {
                return s;
            }
        }
        return IN_PROGRESS;
    }
}
