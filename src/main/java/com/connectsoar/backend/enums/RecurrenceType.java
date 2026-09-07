package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY;

    @JsonCreator
    public static RecurrenceType fromString(String val) {
        if (val == null || val.isBlank()) return NONE;
        String normalized = val.trim().toUpperCase();
        for (RecurrenceType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return NONE;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
