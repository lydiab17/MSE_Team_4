package com.evote.app.votingmanagement.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Domain-Port: nur Interface (keine Implementierung hier).
 */
public interface VoteRepository {
    void save(Vote vote);
    Optional<Vote> findById(String voteId);
    List<Vote> findByVotingId(int votingId);
    boolean existsByVotingIdAndVoterKey(int votingId, String voterKey);

}

