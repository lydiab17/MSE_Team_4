package com.evote.app.vote.events;

import java.time.Instant;

/**
 * Minimales Event-Record. Pseudonym token als String, um Abhängigkeiten zu reduzieren.
 */
public final class VoteSubmittedEvent {
    public final String voteId;
    public final String votingId;
    public final String optionId;
    public final String pseudonymToken;
    public final Instant submittedAt;

    public VoteSubmittedEvent(String voteId, String votingId, String optionId, String pseudonymToken, Instant submittedAt) {
        this.voteId = voteId;
        this.votingId = votingId;
        this.optionId = optionId;
        this.pseudonymToken = pseudonymToken;
        this.submittedAt = submittedAt;
    }
}
