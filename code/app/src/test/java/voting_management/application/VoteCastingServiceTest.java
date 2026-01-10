package voting_management.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.port.AuthPort;
import com.evote.app.votingmanagement.application.services.VoteCastingService;
import com.evote.app.votingmanagement.application.services.VotingCommandService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVoteRepository;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVotingRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
public class VoteCastingServiceTest {

  private InMemoryVotingRepository votingRepo;
  private InMemoryVoteRepository voteRepo;

  private ApplicationEventPublisher eventPublisher;
  private AuthPort authPort;

  private VotingCommandService commandService;
  private VoteCastingService voteCastingService;

  private Clock fixedClock;
  private LocalDate today;

  @BeforeEach
  void setup() {
    votingRepo = new InMemoryVotingRepository();
    voteRepo = new InMemoryVoteRepository();

    today = LocalDate.of(2030, 5, 10);
    fixedClock = Clock.fixed(today.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));

    eventPublisher = mock(ApplicationEventPublisher.class);

    // Default: authentifiziert, gleiches Pseudonym
    authPort = token -> Optional.of(new PseudonymToken("p-1"));

    commandService = new VotingCommandService(votingRepo, eventPublisher, fixedClock);
    voteCastingService =
        new VoteCastingService(votingRepo, voteRepo, authPort, eventPublisher, fixedClock);
  }

  private Set<String> opts(String... vals) {
    Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, vals);
    return set;
  }

  private String firstOptionText(int votingId) {
    Voting v = votingRepo.findById(votingId).orElseThrow();
    return v.getOptionTexts().get(0);
  }

  @Test
  void castVote_success_savesVote_andPublishesEvent() {
    commandService.createVoting(
        30,
        "Abstimmung CastVote",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein"));
    commandService.openVoting(30);

    reset(eventPublisher); // nur VoteSubmittedEvent zählen

    String option = firstOptionText(30);
    voteCastingService.castVote(new CastVoteDto("token-1", 30, option));

    var votes = voteRepo.findByVotingId(30);
    assertEquals(1, votes.size());
    assertTrue(voteRepo.existsByVotingIdAndPseudonym(30, "p-1"));
    assertEquals(option, votes.get(0).getOptionId());

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
    assertEquals("VoteSubmittedEvent", eventCaptor.getValue().getClass().getSimpleName());
  }

  @Test
  void castVote_authFails_throwsIllegalState_andDoesNotPublish() {
    AuthPort failingAuth = token -> Optional.empty();
    VoteCastingService failingSvc =
        new VoteCastingService(votingRepo, voteRepo, failingAuth, eventPublisher, fixedClock);

    commandService.createVoting(
        31,
        "Abstimmung AuthFail",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein"));
    commandService.openVoting(31);

    reset(eventPublisher);

    assertThrows(
        IllegalStateException.class,
        () -> failingSvc.castVote(new CastVoteDto("bad-token", 31, "Ja")));

    // Wichtig: any(Object.class) statt any()
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void castVote_votingNotFound_throwsIllegalArgument() {
    reset(eventPublisher);
    var dto = new CastVoteDto("token-1", 9999, "Ja");
    assertThrows(IllegalArgumentException.class, () -> voteCastingService.castVote(dto));

    // Wichtig: any(Object.class) statt any()
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void castVote_votingNotOpened_throwsIllegalState() {
    commandService.createVoting(
        32,
        "Abstimmung Closed",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein"));

    reset(eventPublisher);

    var dto = new CastVoteDto("token-1", 32, "Ja");
    assertThrows(IllegalStateException.class, () -> voteCastingService.castVote(dto));

    // Wichtig: any(Object.class) statt any()
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void castVote_optionNotInVoting_throwsIllegalArgument() {
    commandService.createVoting(
        33,
        "Abstimmung Option",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein"));
    commandService.openVoting(33);

    reset(eventPublisher);

    var dto = new CastVoteDto("token-1", 33, "Vielleicht");
    assertThrows(IllegalArgumentException.class, () -> voteCastingService.castVote(dto));

    // Wichtig: any(Object.class) statt any()
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void castVote_doubleVote_throwsIllegalState() {
    commandService.createVoting(
        34,
        "Abstimmung Double",
        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
        today.minusDays(1),
        today.plusDays(1),
        opts("Ja", "Nein"));
    commandService.openVoting(34);

    String option = firstOptionText(34);

    reset(eventPublisher);

    voteCastingService.castVote(new CastVoteDto("token-1", 34, option));
    // erstes Publish + Save ok
    verify(eventPublisher, times(1)).publishEvent(any(Object.class));

    reset(eventPublisher);

    assertThrows(
        IllegalStateException.class,
        () -> voteCastingService.castVote(new CastVoteDto("token-1", 34, option)));

    // zweiter Versuch darf nicht publishen
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }
}
