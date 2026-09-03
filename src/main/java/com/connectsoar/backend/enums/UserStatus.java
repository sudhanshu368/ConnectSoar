package com.connectsoar.backend.enums;

public enum UserStatus {
    active,
    inactive,
    suspended;

    public static UserStatus fromString(String val) {
        if (val == null) return active;
        for (UserStatus s : values()) {
            if (s.name().equalsIgnoreCase(val)) {
                return s;
            }
        }
        return active;
    }
}
