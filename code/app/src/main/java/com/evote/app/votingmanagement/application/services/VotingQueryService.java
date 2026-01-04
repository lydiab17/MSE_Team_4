package com.evote.app.votingmanagement.application.services;

import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.domain.model.VoteRepository;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Queries rund um Votings und Auswertungen.
 */
@Service
public class VotingQueryService {

  private final VotingRepository votingRepository;
  private final VoteRepository voteRepository;
  private final Clock clock;

  public VotingQueryService(
          VotingRepository votingRepository,
          VoteRepository voteRepository,
          Clock clock
  ) {
    this.votingRepository = votingRepository;
    this.voteRepository = voteRepository;
    this.clock = clock;
  }

  /**
   * Use Case: Einzelnes Voting holen.
   */
  public Optional<Voting> getVotingById(int id) {
    return votingRepository.findById(id);
  }

  /**
   * Alle aktuell offenen Votings holen.
   */
  public List<Voting> getOpenVotings() {
    Predicate<Voting> isOpen = v -> v.isOpen(clock);
    return votingRepository.findAll().stream()
            .filter(isOpen)
            .toList();
  }

  /**
   * Alle nicht offenen Votings holen.
   */
  public List<Voting> getNotOpenVotings() {
    Predicate<Voting> isOpen = v -> v.isOpen(clock);
    return votingRepository.findAll().stream()
            .filter(isOpen.negate())
            .toList();
  }

  /**
   * Liefert die Ergebnisse (Stimmenanzahl) je Option für ein Voting.
   *
   * @param votingId die ID des Votings
   * @return Liste der Ergebnisse je Option
   * @throws IllegalArgumentException wenn das Voting nicht existiert
   */
  public List<OptionResult> getResultsForVoting(int votingId) {
    Voting voting = votingRepository.findById(votingId)
            .orElseThrow(() -> new IllegalArgumentException("Voting nicht gefunden"));

    var votes = voteRepository.findByVotingId(votingId);

    Map<String, Long> countsByOption = votes.stream()
            .map(v -> v.getOptionId().trim().toLowerCase())
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    return voting.getOptionTexts().stream()
            .map(option -> {
              String key = option.trim().toLowerCase();
              long count = countsByOption.getOrDefault(key, 0L);
              return new OptionResult(option, count);
            })
            .toList();
  }
}
