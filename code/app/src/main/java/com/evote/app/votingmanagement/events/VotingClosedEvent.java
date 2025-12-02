package com.evote.app.votingmanagement.events;

import java.time.Instant;

/**
 * Domain-Event: Ein Voting wurde geschlossen.
 */
public record VotingClosedEvent(
        int id,
        Instant closedAt
) {
}
