package com.evote.app.votingmanagement.infrastructure.repositories;

import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository  // Spring kann dieses Bean dann automatisch injizieren
public class InMemoryVotingRepository implements VotingRepository {

    private final Map<Integer, Voting> store = new HashMap<>();

    @Override
    public void save(Voting voting) {
        store.put(voting.getId(), voting);
    }

    @Override
    public Optional<Voting> findById(int id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Voting> findAll() {
        return new ArrayList<>(store.values());
    }
}
