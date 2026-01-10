package com.evote.app.votingmanagement.events;

import java.time.Instant;

/** Domain-Event: Ein Voting wurde geöffnet (freigeschaltet). */
public record VotingOpenedEvent(int id, Instant openedAt) {}
