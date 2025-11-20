package com.example.evote.vote.domain;

import com.example.evote.shared.PseudonymToken;
import com.example.evote.shared.VotingId;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class Vote {
    private final String id;           // VoteId (simple String/UUID)
    private final VotingId votingId;
    private final String optionId;     // OptionId could be its own VO
    private final PseudonymToken pseudonym;
    private final Instant submittedAt;

    private Vote(String id, VotingId votingId, String optionId, PseudonymToken pseudonym, Instant submittedAt) {
        this.id = Objects.requireNonNull(id);
        this.votingId = Objects.requireNonNull(votingId);
        this.optionId = Objects.requireNonNull(optionId);
        this.pseudonym = Objects.requireNonNull(pseudonym);
        this.submittedAt = Objects.requireNonNull(submittedAt);
    }

    public static Vote createNew(VotingId votingId, String optionId, PseudonymToken pseudonym) {
        return new Vote(UUID.randomUUID().toString(), votingId, optionId, pseudonym, Instant.now());
    }

    // getters
    public String getId() { return id; }
    public VotingId getVotingId() { return votingId; }
    public String getOptionId() { return optionId; }
    public PseudonymToken getPseudonym() { return pseudonym; }
    public Instant getSubmittedAt() { return submittedAt; }
}

