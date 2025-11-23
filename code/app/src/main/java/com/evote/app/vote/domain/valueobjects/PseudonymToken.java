package com.evote.app.vote.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Minimaler Platzhalter für PseudonymToken.
 * Später im Shared Kernel ersetzen.
 */
public final class PseudonymToken {
    private final String token;

    private PseudonymToken(String token) {
        this.token = Objects.requireNonNull(token);
    }

    public static PseudonymToken of(String token) {
        return new PseudonymToken(token);
    }

    public static PseudonymToken generate() {
        return new PseudonymToken(UUID.randomUUID().toString());
    }

    public String value() { return token; }

    @Override
    public String toString() { return token; }
}
