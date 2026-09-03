package com.connectsoar.backend.enums;

public enum Role {
    admin,
    employee;

    public static Role fromString(String val) {
        if (val == null) return employee;
        for (Role r : values()) {
            if (r.name().equalsIgnoreCase(val)) {
                return r;
            }
        }
        return employee;
    }
}
