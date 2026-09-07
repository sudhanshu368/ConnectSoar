package com.connectsoar.backend.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ParticipantRole {
    HOST,
    PARTICIPANT;

    @JsonCreator
    public static ParticipantRole fromString(String val) {
        if (val == null || val.isBlank()) return PARTICIPANT;
        String normalized = val.trim().toUpperCase();
        for (ParticipantRole role : values()) {
            if (role.name().equalsIgnoreCase(normalized)) {
                return role;
            }
        }
        return PARTICIPANT;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}
