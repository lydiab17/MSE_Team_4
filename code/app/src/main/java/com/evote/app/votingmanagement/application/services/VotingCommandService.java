package com.evote.app.votingmanagement.application.services;

import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import com.evote.app.votingmanagement.events.VotingClosedEvent;
import com.evote.app.votingmanagement.events.VotingCreatedEvent;
import com.evote.app.votingmanagement.events.VotingOpenedEvent;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Commands rund um Votings: anlegen, öffnen, schließen.
 */
@Service
public class VotingCommandService {

  private final VotingRepository votingRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  public VotingCommandService(
          VotingRepository votingRepository,
          ApplicationEventPublisher eventPublisher,
          Clock clock
  ) {
    this.votingRepository = votingRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /**
   * Use Case: Neues Voting anlegen.
   */
  public Voting createVoting(
          int id,
          String name,
          String info,
          LocalDate startDate,
          LocalDate endDate,
          Set<String> options
  ) {
    Voting voting = Voting.create(id, name, info, startDate, endDate, options);
    votingRepository.save(voting);

    eventPublisher.publishEvent(new VotingCreatedEvent(id, name, startDate, endDate));
    return voting;
  }

  /**
   * Use Case: Voting öffnen (freischalten).
   */
  public void openVoting(int id) {
    Voting voting = votingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Voting mit ID " + id + " nicht gefunden"));

    voting.setVotingStatus(true);
    votingRepository.save(voting);

    eventPublisher.publishEvent(new VotingOpenedEvent(id, Instant.now(clock)));
  }

  /**
   * Use Case: Voting schließen.
   */
  public void closeVoting(int id) {
    Voting voting = votingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                    "Voting mit ID " + id + " nicht gefunden"));

    voting.setVotingStatus(false);

    // Reihenfolge wie zuvor: erst Event, dann Save
    eventPublisher.publishEvent(new VotingClosedEvent(id, Instant.now(clock)));
    votingRepository.save(voting);
  }
}
