package com.evote.app.vote.infrastructure;

import com.evote.app.vote.domain.vote;
import com.evote.app.vote.domain.voteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread-safe In-Memory implementation of VoteRepository.
 * Suitable for tests and local prototyping.
 */
public class InMemoryVoteRepository implements voteRepository {

    // key = voteId, value = Vote
    private final Map<String, vote> store = new ConcurrentHashMap<>();

    @Override
    public void save(vote vote) {
        // overwrite if exists (idempotent save)
        store.put(vote.getId(), vote);
    }

    @Override
    public Optional<vote> findById(String voteId) {
        return Optional.ofNullable(store.get(voteId));
    }

    @Override
    public List<vote> findByVotingId(String votingId) {
        // collect to new list to avoid exposing internal map values view
        return store.values().stream()
                .filter(v -> v.getVotingId() != null && votingId.equals(v.getVotingId().value()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public boolean existsByVotingIdAndPseudonym(String votingId, String pseudonymToken) {
        return store.values().stream()
                .anyMatch(v -> v.getVotingId() != null
                        && votingId.equals(v.getVotingId().value())
                        && v.getPseudonym() != null
                        && pseudonymToken.equals(v.getPseudonym().value()));
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
    public long countByVotingId(String votingId) {
        return store.values().stream()
                .filter(v -> v.getVotingId() != null && votingId.equals(v.getVotingId().value()))
                .count();
    }
}
