package com.evote.app.vote.application;

import com.evote.app.vote.domain.PseudonymToken;
import com.evote.app.vote.domain.vote;
import com.evote.app.vote.domain.voteRepository;
import com.evote.app.vote.events.VoteSubmittedEvent;
import com.evote.app.vote.domain.port.AuthPort;
import com.evote.app.vote.domain.auth.VerificationStatus;

import java.time.Instant;

/**
 * CastVoteUseCase (vereinfachte Variante, OHNE VotingRepository).
 * Prüft nur Authentifizierung, Doppelstimmen, speichert Vote und publisht Event.
 *
 * Hinweis:
 * - Ohne VotingRepository erfolgt keine Prüfung, ob die Abstimmung existiert oder offen ist.
 * - Das ist für frühe Tests/Prototypen ok, für Produktion solltest du die Verfügbarkeitsprüfung wieder einbauen
 *   (entweder via VotingRepository in einem anderen Bounded Context oder via ein leichtes Availability-Port).
 */
public class CastVoteUseCase {

    private final AuthPort authPort;
    private final voteRepository voteRepository;
    private final EventPublisher eventPublisher;

    public CastVoteUseCase(AuthPort authPort,
                           voteRepository voteRepository,
                           EventPublisher eventPublisher) {
        this.authPort = authPort;
        this.voteRepository = voteRepository;
        this.eventPublisher = eventPublisher;
    }

    public void execute(CastVoteDto dto) {
        // 1) Authentifizieren (Domain-Port)
        VerificationStatus status = authPort.verify(dto.authToken);
        if (status == null || !status.isVerified()) {
            throw new RuntimeException("Not authenticated");
        }
        String pseudonym = status.getPseudonym();

        // 2) Vote erstellen (Domain)
        vote vote = com.evote.app.vote.domain.vote.createNew(
                com.evote.app.vote.domain.VotingId.of(dto.votingId),
                dto.optionId,
                PseudonymToken.of(pseudonym)
        );

        // 3) Duplicate-Check (synchron, via VoteRepository)
        if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId, pseudonym)) {
            throw new RuntimeException("Duplicate vote");
        }

        // 4) Persistieren
        voteRepository.save(vote);

        // 5) Event publizieren
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


//-----------------------
// Code mit VotingRepository
//Wichtige Hinweise / Risiken
//
//Keine Prüfung der Abstimmung: Ohne VotingRepository weiß das UseCase nicht, ob dto.votingId gültig ist oder ob die Abstimmung offen/geschlossen ist. Das kann zu ungültigen Votes führen.
//
//Empfehlung für produktive Nutzung:
//
//kurz- bis mittelfristig: füge ein kleines Interface VotingAvailabilityPort (oder VotingRepository) hinzu, das boolean isVotingOpen(String votingId) liefert; implementiere das im Voting-Context / infra.
//
//langfristig: gültigkeits- und optionsprüfung gehört fachlich in den Voting-Context (Bounded Context) — am saubersten per Event/Port-Kommunikation.


//package com.evote.app.vote.application;
//
//import com.evote.app.vote.domain.PseudonymToken;
//import com.evote.app.vote.domain.Vote;
//import com.evote.app.vote.domain.voteRepository;
//import com.evote.app.vote.events.VoteSubmittedEvent;
//import com.evote.app.vote.domain.port.AuthPort;
//import com.evote.app.vote.domain.auth.VerificationStatus;
//
//import java.time.Instant;
//
///**
// * CastVoteUseCase (vereinfachte Variante, OHNE VotingRepository).
// * Prüft nur Authentifizierung, Doppelstimmen, speichert Vote und publisht Event.
// *
// * Hinweis:
// * - Ohne VotingRepository erfolgt keine Prüfung, ob die Abstimmung existiert oder offen ist.
// * - Das ist für frühe Tests/Prototypen ok, für Produktion solltest du die Verfügbarkeitsprüfung wieder einbauen
// *   (entweder via VotingRepository in einem anderen Bounded Context oder via ein leichtes Availability-Port).
// */
//public class CastVoteUseCase {
//
//    private final AuthPort authPort;
//    private final VoteRepository voteRepository;
//    private final EventPublisher eventPublisher;
//
//    public CastVoteUseCase(AuthPort authPort,
//                           VoteRepository voteRepository,
//                           EventPublisher eventPublisher) {
//        this.authPort = authPort;
//        this.voteRepository = voteRepository;
//        this.eventPublisher = eventPublisher;
//    }
//
//    public void execute(CastVoteDto dto) {
//        // 1) Authentifizieren (Domain-Port)
//        VerificationStatus status = authPort.verify(dto.authToken);
//        if (status == null || !status.isVerified()) {
//            throw new RuntimeException("Not authenticated");
//        }
//        String pseudonym = status.getPseudonym();
//
//        // 2) Vote erstellen (Domain)
//        Vote vote = Vote.createNew(
//                com.evote.app.vote.domain.VotingId.of(dto.votingId),
//                dto.optionId,
//                PseudonymToken.of(pseudonym)
//        );
//
//        // 3) Duplicate-Check (synchron, via VoteRepository)
//        if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId, pseudonym)) {
//            throw new RuntimeException("Duplicate vote");
//        }
//
//        // 4) Persistieren
//        voteRepository.save(vote);
//
//        // 5) Event publizieren
//        VoteSubmittedEvent event = new VoteSubmittedEvent(
//                vote.getId(),
//                dto.votingId,
//                dto.optionId,
//                pseudonym,
//                Instant.now()
//        );
//        eventPublisher.publish(event);
//    }
//}
