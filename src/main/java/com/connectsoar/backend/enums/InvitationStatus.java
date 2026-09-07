package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED,
    CANCELLED;

    @JsonCreator
    public static InvitationStatus fromString(String val) {
        if (val == null || val.isBlank()) return PENDING;
        String normalized = val.trim().toUpperCase();
        for (InvitationStatus status : values()) {
            if (status.name().equalsIgnoreCase(normalized)) {
                return status;
            }
        }
        return PENDING;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
