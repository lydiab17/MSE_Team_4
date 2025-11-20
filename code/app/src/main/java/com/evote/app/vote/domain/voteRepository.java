package com.example.evote.vote.domain;

import java.util.List;
import java.util.Optional;

public interface VoteRepository {
    void save(Vote vote);
    Optional<Vote> findById(String voteId);
    List<Vote> findByVotingId(String votingId); // for projections / results
    boolean existsByVotingIdAndPseudonym(String votingId, String pseudonym); // used for duplicate-check (sync)
}
