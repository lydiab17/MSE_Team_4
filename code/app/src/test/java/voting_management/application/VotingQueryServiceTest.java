package voting_management.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.services.VotingCommandService;
import com.evote.app.votingmanagement.application.services.VotingQueryService;
import com.evote.app.votingmanagement.domain.model.Vote;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVoteRepository;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVotingRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

public class VotingQueryServiceTest {

  private InMemoryVotingRepository votingRepo;
  private InMemoryVoteRepository voteRepo;

  private VotingCommandService commandService;
  private VotingQueryService queryService;

  private Clock fixedClock;
  private LocalDate today;

  @BeforeEach
  void setup() {
    votingRepo = new InMemoryVotingRepository();
    voteRepo = new InMemoryVoteRepository();

    today = LocalDate.of(2030, 5, 10);
    fixedClock = Clock.fixed(today.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    // CommandService braucht Publisher -> Dummy Mock reicht
    ApplicationEventPublisher dummyPublisher = mock(ApplicationEventPublisher.class);
    commandService = new VotingCommandService(votingRepo, dummyPublisher, fixedClock);

    queryService = new VotingQueryService(votingRepo, voteRepo, fixedClock);
  }

  private Set<String> opts(String... vals) {
    Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, vals);
    return set;
  }

  @Test
  void getVotingById_returnsEmpty_whenNotFound() {
    assertTrue(queryService.getVotingById(12345).isEmpty());
  }

  @Test
  void getVotingById_returnsVoting_whenFound() {
    commandService.createVoting(
        11,
        "Abstimmung GetById",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today,
        today.plusDays(5),
        opts("Ja", "Nein"));

    var opt = queryService.getVotingById(11);
    assertTrue(opt.isPresent());
    assertEquals(11, opt.get().getId());
  }

  @Test
  void getOpenVotings_returnsOnlyOpenOnes() {
    Voting v1 =
        commandService.createVoting(
            4,
            "Abstimmung Offen",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein"));
    v1.setVotingStatus(true);
    votingRepo.save(v1);

    Voting v2 =
        commandService.createVoting(
            5,
            "Abstimmung Geschlossen",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein"));
    votingRepo.save(v2);

    var openVotings = queryService.getOpenVotings();

    assertEquals(1, openVotings.size());
    assertEquals(4, openVotings.get(0).getId());
  }

  @Test
  void getNotOpenVotings_returnsOnlyThoseNotOpen() {
    Voting open =
        commandService.createVoting(
            7,
            "Abstimmung Offen 2",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein"));
    open.setVotingStatus(true);
    votingRepo.save(open);

    Voting closed =
        commandService.createVoting(
            8,
            "Abstimmung Zu",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(100),
            today.plusDays(100),
            opts("Ja", "Nein"));
    closed.setVotingStatus(false);
    votingRepo.save(closed);

    var notOpen = queryService.getNotOpenVotings();
    assertEquals(1, notOpen.size());
    assertEquals(8, notOpen.get(0).getId());
  }

  @Test
  void getResultsForVoting_countsVotesPerOption_caseInsensitive_andIncludesZeroCounts() {
    commandService.createVoting(
        20,
        "Abstimmung Results",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein", "Enthaltung"));

    voteRepo.save(Vote.createNew(20, "Ja", "p1"));
    voteRepo.save(Vote.createNew(20, "ja", "p2"));
    voteRepo.save(Vote.createNew(20, "Nein", "p3"));
    voteRepo.save(Vote.createNew(999, "Ja", "otherVoting"));

    List<OptionResult> results = queryService.getResultsForVoting(20);
    assertEquals(3, results.size());

    Map<String, Long> map = new HashMap<>();
    for (OptionResult r : results) {
      map.put(r.option(), r.count());
    }

    assertEquals(2L, map.get("Ja"));
    assertEquals(1L, map.get("Nein"));
    assertEquals(0L, map.get("Enthaltung"));
  }

  @Test
  void getResultsForVoting_unknownVoting_throwsException() {
    assertThrows(IllegalArgumentException.class, () -> queryService.getResultsForVoting(404));
  }
}
