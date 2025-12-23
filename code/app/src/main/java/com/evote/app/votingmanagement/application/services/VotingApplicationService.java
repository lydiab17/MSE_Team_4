package com.evote.app.votingmanagement.application.services;

import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.port.AuthPort;
import com.evote.app.votingmanagement.domain.model.Vote;
import com.evote.app.votingmanagement.domain.model.VoteRepository;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;



/**
 * Anwendungsschicht-Service rund um Votings und Votes.
 *
 * Verantwortlichkeiten:
 * <ul>
 *   <li>Abstimmungen anlegen, öffnen, schließen, abfragen</li>
 *   <li>Stimmen abgeben (ohne Auth / Pseudonymisierung – vorläufig)</li>
 * </ul>
 */
@Service
public class VotingApplicationService {

    private final VotingRepository votingRepository;
    private final VoteRepository voteRepository;
    private final AuthPort authPort;

    public VotingApplicationService(VotingRepository votingRepository,
                                    VoteRepository voteRepository,
                                    AuthPort authPort) {
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.authPort = authPort;
    }



    // =========================================================
    //  Voting-Use-Cases (wie bisher)
    // =========================================================

    /**
     * Use Case: Neues Voting anlegen.
     */
    public Voting createVoting(int id,
                               String name,
                               String info,
                               LocalDate startDate,
                               LocalDate endDate,
                               Set<String> options) {

        // Domain kümmert sich um alle Regeln / Validierung
        Voting voting = Voting.create(id, name, info, startDate, endDate, options);

        // dann speichern wir es über das Repository
        votingRepository.save(voting);

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
    }

    /**
     * Use Case: Voting schließen.
     */
    public void closeVoting(int id) {
        Voting voting = votingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Voting mit ID " + id + " nicht gefunden"));

        voting.setVotingStatus(false);
        votingRepository.save(voting);
    }

    /**
     * Use Case: Einzelnes Voting holen.
     */
    public Optional<Voting> getVotingById(int id) {
        return votingRepository.findById(id);
    }

    /**
     * Beispiel: Alle aktuell offenen Votings holen.
     * (hier nehmen wir die System-Uhr; in Tests könntest du eine andere Uhr verwenden)
     */
    public List<Voting> getOpenVotings(Clock clock) {
        return votingRepository.findAll().stream()
                .filter(v -> v.isOpen(clock))
                .toList();
    }

    /**
     * Beispiel: Alle nicht offenen Votings holen.
     * (hier nehmen wir die System-Uhr; in Tests könntest du eine andere Uhr verwenden)
     */
    public List<Voting> getNotOpenVotings() {
        return votingRepository.findAll().stream()
                .filter(v -> !v.isVotingStatus())
                .toList();
    }

    public List<OptionResult> getResultsForVoting(int votingId) {
        // Sicherstellen, dass das Voting existiert
        Voting voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));

        // Alle Votes zu diesem Voting laden
        var votes = voteRepository.findByVotingId(votingId);

        // Für jede Option die Anzahl der Stimmen zählen
        return voting.getOptionTexts().stream()
                .map(option -> {
                    long count = votes.stream()
                            .filter(v -> v.getOptionId().equalsIgnoreCase(option))
                            .count();
                    return new OptionResult(option, count);
                })
                .toList();
    }


    // =========================================================
    //  Vote-Use-Case: Stimme abgeben (vereinfachte Variante)
    // =========================================================

    /**
     * Use Case: Stimme abgeben (ohne Auth / Events).
     *
     * Schritte:
     * <ol>
     *   <li>Voting laden</li>
     *   <li>prüfen, ob Voting geöffnet ist</li>
     *   <li>prüfen, ob Option existiert</li>
     *   <li>prüfen, ob dieser "Wähler" schon abgestimmt hat</li>
     *   <li>Vote erzeugen und speichern</li>
     * </ol>
     *
     * Hinweis: {@code dto.authToken()} wird hier vorläufig
     * als einfacher voterKey verwendet. Später wird das durch
     * ein echtes Pseudonym/Token aus dem citizen_management ersetzt.
     */
    public void castVote(CastVoteDto dto) {
        // 0) Token -> Pseudonym
        PseudonymToken pseudonym = authPort
                .verifyAndGetPseudonym(new AuthToken(dto.authToken))
                .orElseThrow(() -> new IllegalStateException("Not authenticated"));

        // 1) Voting laden
        Voting voting = getVotingById(dto.votingId)
                .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));

        // 2) Prüfen, ob Voting geöffnet ist
        if (!voting.isVotingStatus()) {
            throw new IllegalStateException("Voting ist nicht geöffnet");
        }

        // 3) Prüfen, ob Option zum Voting gehört
        boolean optionExists = voting.getOptionTexts().stream()
                .anyMatch(o -> o.equalsIgnoreCase(dto.optionId));
        if (!optionExists) {
            throw new IllegalArgumentException("Option existiert nicht in diesem Voting");
        }

        // 4) Doppelabstimmung verhindern (jetzt mit Pseudonym)
        if (voteRepository.existsByVotingIdAndPseudonym(dto.votingId, pseudonym.value())) {
            throw new IllegalStateException("Dieser Wähler hat bereits abgestimmt");
        }

        // 5) Vote erstellen (Domain) – statt voterKey jetzt pseudonym
        Vote vote = Vote.createNew(dto.votingId, dto.optionId, pseudonym.value());

        // 6) Persistieren
        voteRepository.save(vote);
    }

}
