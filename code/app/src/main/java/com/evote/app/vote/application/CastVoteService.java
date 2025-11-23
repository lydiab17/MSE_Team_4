package com.evote.app.vote.application;

import com.evote.app.vote.domain.Vote;
import com.evote.app.vote.domain.VoteRepository;
import com.evote.app.vote.domain.valueobjects.PseudonymToken;
import com.evote.app.vote.domain.valueobjects.auth.VerificationStatus;
import com.evote.app.vote.domain.valueobjects.port.AuthPort;
import com.evote.app.vote.events.VoteSubmittedEvent;
import com.evote.app.votingmanagement.application.VotingApplicationService;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Anwendungsschicht-Use-Case zum Abgeben einer Stimme.
 *
 * Schritte:
 * <ol>
 *   <li>Authentifizierung über {@link AuthPort}</li>
 *   <li>Voting über {@link VotingApplicationService} laden</li>
 *   <li>Prüfen, ob Voting geöffnet ist</li>
 *   <li>Prüfen, ob die gewählte Option im Voting existiert</li>
 *   <li>Prüfen, ob der Wähler bereits abgestimmt hat</li>
 *   <li>Vote als Domain-Objekt erzeugen und speichern</li>
 *   <li>Event {@link VoteSubmittedEvent} publizieren</li>
 * </ol>
 */
@Service
public class CastVoteService {

  private final AuthPort authPort;
  private final VoteRepository voteRepository;
  private final EventPublisher eventPublisher;
  private final VotingApplicationService votingService;

  public CastVoteService(AuthPort authPort,
                         VoteRepository voteRepository,
                         EventPublisher eventPublisher,
                         VotingApplicationService votingService) {
    this.authPort = authPort;
    this.voteRepository = voteRepository;
    this.eventPublisher = eventPublisher;
    this.votingService = votingService;
  }

  public void execute(CastVoteDto dto) {
    // 1) Authentifizieren (Domain-Port)
    VerificationStatus status = authPort.verify(dto.authToken);
    if (status == null || !status.isVerified()) {
      throw new RuntimeException("Not authenticated");
    }
    String pseudonym = status.getPseudonym();

    // 2) Voting laden (aus Bounded Context votingmanagement)
    var voting = votingService.getVotingById(dto.votingId)
            .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));

    // 3) Prüfen, ob Voting geöffnet ist
    if (!voting.isVotingStatus()) {
      throw new IllegalStateException("Voting ist nicht geöffnet");
    }

    // 4) Prüfen, ob Option zum Voting gehört
    boolean optionExists = voting.getOptionTexts().stream()
            .anyMatch(o -> o.equalsIgnoreCase(dto.optionId));
    if (!optionExists) {
      throw new IllegalArgumentException("Option existiert nicht in diesem Voting");
    }

    // 5) Duplicate-Check (synchron, via VoteRepository)
    if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId, pseudonym)) {
      throw new RuntimeException("Duplicate vote");
    }

    // 6) Vote erstellen (Domain)
    Vote vote = Vote.createNew(
            dto.votingId,
            dto.optionId,
            PseudonymToken.of(pseudonym)
    );

    // 7) Persistieren
    voteRepository.save(vote);

    // 8) Event publizieren
    VoteSubmittedEvent event = new VoteSubmittedEvent(
            vote.getId(),
            dto.votingId,
            dto.optionId,
            pseudonym,
            Instant.now()
    );
    eventPublisher.publish(event);
  }
}
