package com.evote.app.voting_management.infrastructure.repositories;

import com.evote.app.voting_management.domain.model.Voting;
import com.evote.app.voting_management.domain.model.VotingRepository;
import org.springframework.stereotype.Repository;

import java.time.Clock;
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
    public List<Voting> findAllOpen() {
        Clock clock = Clock.systemDefaultZone();
        List<Voting> result = new ArrayList<>();

        for (Voting v : store.values()) {
            if (v.isOpen(clock)) {
                result.add(v);
            }
        }
        return result;
    }

    @Override
    public List<Voting> findAll() {
        return new ArrayList<>(store.values());
    }
}
