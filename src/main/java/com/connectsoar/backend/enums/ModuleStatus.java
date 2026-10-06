package com.connectsoar.backend.enums;

public enum ModuleStatus {
    TODO,
    IN_PROGRESS,
    REVIEW,
    COMPLETED,
    BLOCKED;

    public static ModuleStatus fromString(String val) {
        if (val == null) return TODO;
        for (ModuleStatus s : values()) {
            if (s.name().equalsIgnoreCase(val)) {
                return s;
            }
        }
        return TODO;
    }
}
