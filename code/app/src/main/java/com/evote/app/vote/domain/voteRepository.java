package com.evote.app.vote.domain;

import java.util.List;
import java.util.Optional;

/**
 * Domain-Port: nur Interface (keine Implementierung hier).
 */
public interface voteRepository {
    void save(vote vote);
    Optional<vote> findById(String voteId);
    List<vote> findByVotingId(String votingId);
    boolean existsByVotingIdAndPseudonym(String votingId, String pseudonymToken);
}

