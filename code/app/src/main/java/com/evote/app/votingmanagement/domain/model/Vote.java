package com.evote.app.votingmanagement.domain.model;

import com.evote.app.votingmanagement.domain.valueobjects.PseudonymToken;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Einfaches immutable Vote-Aggregate (minimal).
 */
public final class Vote {

    private final String id;
    private final int votingId;
    private final String optionId;
    private final String voterKey;   // früher: PseudonymToken
    private final Instant submittedAt;

    private Vote(String id,
                 int votingId,
                 String optionId,
                 String voterKey,
                 Instant submittedAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.votingId = votingId;
        this.optionId = Objects.requireNonNull(optionId, "optionId");
        this.voterKey = Objects.requireNonNull(voterKey, "voterKey");
        this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
    }

    public static Vote createNew(int votingId, String optionId, String voterKey) {
        return new Vote(
                UUID.randomUUID().toString(),
                votingId,
                optionId,
                voterKey,
                Instant.now()
        );
    }

    public String getId() { return id; }

    public int getVotingId() { return votingId; }

    public String getOptionId() { return optionId; }

    public String getVoterKey() { return voterKey; }

    public Instant getSubmittedAt() { return submittedAt; }
}

