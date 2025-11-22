package com.evote.app.vote.events;

import java.time.Instant;

public class VotePersistedEvent {
    public final String voteId;
    public final String votingId;
    public final Instant recordedAt;
    public final String storageHash; // optional, can be null

    public VotePersistedEvent(String voteId, String votingId, Instant recordedAt, String storageHash) {
        this.voteId = voteId;
        this.votingId = votingId;
        this.recordedAt = recordedAt;
        this.storageHash = storageHash;
    }
}
