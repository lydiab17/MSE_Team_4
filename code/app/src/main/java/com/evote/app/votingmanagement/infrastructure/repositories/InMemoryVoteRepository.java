package com.evote.app.votingmanagement.infrastructure.repositories;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.evote.app.votingmanagement.domain.model.Vote;
import com.evote.app.votingmanagement.domain.model.VoteRepository;
import org.springframework.stereotype.Repository;

/**
 * Thread-safe In-Memory implementation of VoteRepository.
 * Suitable for tests and local prototyping.
 */
@Repository
public class InMemoryVoteRepository implements VoteRepository {

  // key = voteId, value = Vote
  private final Map<String, Vote> store = new ConcurrentHashMap<>();

  @Override
  public void save(Vote vote) {
    // overwrite if exists (idempotent save)
    store.put(vote.getId(), vote);
  }

  @Override
  public Optional<Vote> findById(String voteId) {
    return Optional.ofNullable(store.get(voteId));
  }

  @Override
  public List<Vote> findByVotingId(int votingId) {
    // collect to new list to avoid exposing internal map values view
    return store.values().stream()
            .filter(v -> v.getVotingId() == votingId)
            .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public boolean existsByVotingIdAndVoterKey(int votingId, String voterKey) {
      return store.values().stream()
              .anyMatch(v -> v.getVotingId() == votingId
                      && voterKey.equals(v.getVoterKey()));
  }


  /**
   * Helper: clear repository (useful in tests).
   */
  public void clear() {
    store.clear();
  }

  /**
   * Helper: count votes for a votingId (useful in assertions).
   */
  public long countByVotingId(int votingId) {
    return store.values().stream()
            .filter(v -> votingId == v.getVotingId())
            .count();
  }
}
