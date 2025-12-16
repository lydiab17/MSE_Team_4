package com.evote.app.sharedkernel;

import java.util.Objects;

/** Wrappt ein Auth-Token (z.B. JWT) als Value Object. */
public record AuthToken(String value) {
    public AuthToken {
        Objects.requireNonNull(value, "AuthToken darf nicht null sein");
        if (value.isBlank()) {
            throw new IllegalArgumentException("AuthToken darf nicht leer sein");
        }
    }
}
