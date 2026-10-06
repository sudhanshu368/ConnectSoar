package com.connectsoar.backend.enums;

public enum ModuleType {
    FRONTEND,
    BACKEND,
    FULL_STACK,
    GENERAL;

    public static ModuleType fromString(String val) {
        if (val == null) return FULL_STACK;
        for (ModuleType t : values()) {
            if (t.name().equalsIgnoreCase(val)) {
                return t;
            }
        }
        return FULL_STACK;
    }
}
