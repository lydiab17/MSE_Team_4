package com.evote.app.vote.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Einfaches immutable Vote-Aggregate (minimal).
 */
public final class vote {
    private final String id;
    private final VotingId votingId;
    private final String optionId;
    private final PseudonymToken pseudonym;
    private final Instant submittedAt;

    private vote(String id, VotingId votingId, String optionId, PseudonymToken pseudonym, Instant submittedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.votingId = Objects.requireNonNull(votingId, "votingId");
        this.optionId = Objects.requireNonNull(optionId, "optionId");
        this.pseudonym = Objects.requireNonNull(pseudonym, "pseudonym");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
    }

    public static vote createNew(VotingId votingId, String optionId, PseudonymToken pseudonym) {
        return new vote(UUID.randomUUID().toString(), votingId, optionId, pseudonym, Instant.now());
    }

    // getters
    public String getId() { return id; }
    public VotingId getVotingId() { return votingId; }
    public String getOptionId() { return optionId; }
    public PseudonymToken getPseudonym() { return pseudonym; }
    public Instant getSubmittedAt() { return submittedAt; }
}
