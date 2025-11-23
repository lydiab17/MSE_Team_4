package com.evote.app.votingmanagement.application;

import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class VotingApplicationService {

    private final VotingRepository votingRepository;

    public VotingApplicationService(VotingRepository votingRepository) {
        this.votingRepository = votingRepository;
    }

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
                .orElseThrow(() -> new IllegalArgumentException("Voting mit ID " + id + " nicht gefunden"));

        voting.setVotingStatus(true);
        votingRepository.save(voting);
    }

    /**
     * Use Case: Voting schließen.
     */
    public void closeVoting(int id) {
        Voting voting = votingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Voting mit ID " + id + " nicht gefunden"));

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
}
