package com.evote.app.votingmanagement.application.services;

import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.port.AuthPort;
import com.evote.app.votingmanagement.domain.model.Vote;
import com.evote.app.votingmanagement.domain.model.VoteRepository;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import com.evote.app.votingmanagement.events.VoteSubmittedEvent;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Use Case: Stimme abgeben.
 */
@Service
public class VoteCastingService {

  private final VotingRepository votingRepository;
  private final VoteRepository voteRepository;
  private final AuthPort authPort;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;

  public VoteCastingService(
          VotingRepository votingRepository,
          VoteRepository voteRepository,
          AuthPort authPort,
          ApplicationEventPublisher eventPublisher,
          Clock clock
  ) {
    this.votingRepository = votingRepository;
    this.voteRepository = voteRepository;
    this.authPort = authPort;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }
  /**
   * Use Case: Stimme abgeben (ohne Auth / Events).
   *
   * <p>Schritte:
   * <ol>
   *   <li>Voting laden</li>
   *   <li>prüfen, ob Voting geöffnet ist</li>
   *   <li>prüfen, ob Option existiert</li>
   *   <li>prüfen, ob dieser "Wähler" schon abgestimmt hat</li>
   *   <li>Vote erzeugen und speichern</li>
   * </ol>
   *
   * <p>Hinweis: {@code dto.authToken()} wird hier vorläufig
   * als einfacher voterKey verwendet. Später wird das durch
   * ein echtes Pseudonym/Token aus dem citizen_management ersetzt.
   *
   * @param dto Eingabedaten zum Abstimmen
   */
  public void castVote(CastVoteDto dto) {
    PseudonymToken pseudonym = authPort
            .verifyAndGetPseudonym(new AuthToken(dto.authToken()))
            .orElseThrow(() -> new IllegalStateException("Not authenticated"));

    Voting voting = votingRepository.findById(dto.votingId())
            .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));

    if (!voting.isVotingStatus()) {
      throw new IllegalStateException("Voting ist nicht geöffnet");
    }

    boolean optionExists = voting.getOptionTexts().stream()
            .anyMatch(o -> o.equalsIgnoreCase(dto.optionId()));
    if (!optionExists) {
      throw new IllegalArgumentException("Option existiert nicht in diesem Voting");
    }

    if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId(), pseudonym.value())) {
      throw new IllegalStateException("Dieser Wähler hat bereits abgestimmt");
    }

    Vote vote = Vote.createNew(dto.votingId(), dto.optionId(), pseudonym.value());

    // Reihenfolge wie zuvor: erst Event, dann Save
    eventPublisher.publishEvent(new VoteSubmittedEvent(
            dto.votingId(), dto.optionId(), pseudonym.value(), Instant.now(clock)
    ));

    voteRepository.save(vote);
  }
}
