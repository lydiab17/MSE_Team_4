package com.evote.app.votingmanagement.events;

import java.time.Instant;

/**
 * Minimales Event-Objekt.
 *
 * <p>Pseudonym-Token als String, um Abhängigkeiten zu reduzieren.
 */
public record VoteSubmittedEvent(
        int votingId,
        String optionId,
        String pseudonymToken,
        Instant submittedAt
) {
}

