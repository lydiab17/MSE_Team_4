package com.evote.app.voting_management.domain.model;

import java.util.List;
import java.util.Optional;

public interface VotingRepository {

    void save(Voting voting);

    Optional<Voting> findById(int id);

    List<Voting> findAllOpen();   // oder was ihr fachlich braucht

    List<Voting> findAll();
}
