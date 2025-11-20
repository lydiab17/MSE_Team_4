// events/VoteSubmittedEvent.java
package com.example.evote.vote.events;

import com.example.evote.shared.PseudonymToken;
import java.time.Instant;

public record VoteSubmittedEvent(String voteId, String votingId, String optionId, PseudonymToken pseudonym, Instant submittedAt) { }

// events/VotePersistedEvent.java
package com.example.evote.vote.events;

import java.time.Instant;

public record VotePersistedEvent(String voteId, String votingId, Instant recordedAt, String storageHash) { }
