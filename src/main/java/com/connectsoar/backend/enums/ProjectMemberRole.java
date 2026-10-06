package com.connectsoar.backend.enums;

public enum ProjectMemberRole {
    MANAGER,
    FRONTEND,
    BACKEND,
    FULL_STACK,
    MEMBER;

    public static ProjectMemberRole fromString(String val) {
        if (val == null) return FULL_STACK;
        for (ProjectMemberRole r : values()) {
            if (r.name().equalsIgnoreCase(val)) {
                return r;
            }
        }
        return FULL_STACK;
    }
}
