package com.evote.app.votingmanagement.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Repository-Schnittstelle für das Verwalten von {@link Voting}-Aggregaten.
 */
public interface VotingRepository {

  void save(Voting voting);

  Optional<Voting> findById(int id);

  List<Voting> findAll();
}
