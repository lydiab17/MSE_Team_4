package com.evote.app.votingmanagement.infrastructure.repositories;

import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Einfache In-Memory-Implementierung des {@link VotingRepository}.
 * Dient vor allem zu Test- und Entwicklungszwecken.
 */
@Repository // Spring kann dieses Bean dann automatisch injizieren
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
