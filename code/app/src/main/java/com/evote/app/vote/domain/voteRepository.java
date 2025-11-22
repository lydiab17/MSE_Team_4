package com.evote.app.vote.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain-Port: nur Interface (keine Implementierung hier).
 */
public interface voteRepository {
    void save(Vote vote);
    Optional<Vote> findById(String voteId);
    List<Vote> findByVotingId(String votingId);
    boolean existsByVotingIdAndPseudonym(String votingId, String pseudonymToken);
}

