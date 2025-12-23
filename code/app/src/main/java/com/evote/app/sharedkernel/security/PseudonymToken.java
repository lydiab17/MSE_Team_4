package com.evote.app.sharedkernel.security;

import java.util.Objects;

/**
 * Pseudonymer Identifier eines Bürgers (ohne Klardaten wie E-Mail).
 * Wird z.B. aus einem verifizierten JWT abgeleitet.
 */
public record PseudonymToken(String value) {

    public PseudonymToken {
        Objects.requireNonNull(value, "PseudonymToken darf nicht null sein");
        if (value.isBlank()) {
            throw new IllegalArgumentException("PseudonymToken darf nicht leer sein");
        }
    }
}
