package com.example.evote.vote.infrastructure;

import com.example.evote.vote.domain.Vote;
import com.example.evote.vote.domain.VoteRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryVoteRepository implements VoteRepository {
    private final Map<String, Vote> store = new ConcurrentHashMap<>();

    @Override
    public void save(Vote vote) {
        store.put(vote.getId(), vote);
    }

    @Override
    public Optional<Vote> findById(String voteId) {
        return Optional.ofNullable(store.get(voteId));
    }

    @Override
    public List<Vote> findByVotingId(String votingId) {
        return store.values().stream().filter(v -> v.getVotingId().value().equals(votingId)).collect(Collectors.toList());
    }

    @Override
    public boolean existsByVotingIdAndPseudonym(String votingId, String pseudonym) {
        return store.values().stream().anyMatch(v -> v.getVotingId().value().equals(votingId) && v.getPseudonym().value().equals(pseudonym));
    }
}

