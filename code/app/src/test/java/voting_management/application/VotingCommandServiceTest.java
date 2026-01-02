package voting_management.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.evote.app.votingmanagement.application.services.VotingCommandService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVoteRepository;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVotingRepository;
import java.time.Clock;
import java.time.Instant;
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
public class VotingCommandServiceTest {

  private InMemoryVotingRepository votingRepo;
  @SuppressWarnings("unused")
  private InMemoryVoteRepository voteRepo;

  private ApplicationEventPublisher eventPublisher; // jetzt Spring-Typ
  private VotingCommandService commandService;

  private Clock fixedClock;
  private LocalDate today;

  @BeforeEach
  void setup() {
    votingRepo = new InMemoryVotingRepository();
    voteRepo = new InMemoryVoteRepository();

    today = LocalDate.of(2030, 5, 10);
    fixedClock = Clock.fixed(
            today.atStartOfDay(ZoneId.of("UTC")).toInstant(),
            ZoneId.of("UTC")
    );

    eventPublisher = mock(ApplicationEventPublisher.class);
    commandService = new VotingCommandService(votingRepo, eventPublisher, fixedClock);
  }

  private Set<String> opts(String... vals) {
    Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, vals);
    return set;
  }

  @Test
  void createVoting_savesVotingInRepository_andPublishesCreatedEvent() {
    Voting created = commandService.createVoting(
            1,
            "Abstimmung 2030",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today,
            today.plusDays(7),
            opts("Ja", "Nein")
    );

    assertNotNull(created);
    Optional<Voting> fromRepo = votingRepo.findById(1);
    assertTrue(fromRepo.isPresent(), "Voting sollte im Repository gespeichert werden");
    assertEquals("Abstimmung 2030", fromRepo.get().getName());

    // Event published?
    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
    assertEquals("VotingCreatedEvent", eventCaptor.getValue().getClass().getSimpleName());
  }

  @Test
  void createVoting_invalidName_throwsException_andDoesNotPublish() {
    assertThrows(IllegalArgumentException.class, () ->
            commandService.createVoting(
                    2,
                    "zuKurz",
                    "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                    today,
                    today.plusDays(1),
                    opts("Ja", "Nein")
            )
    );

    verify(eventPublisher, never()).publishEvent(any());
  }

  @Test
  void openVoting_changesStatusToTrue_andPublishesOpenedEvent() {
    commandService.createVoting(
            3,
            "Abstimmung Status",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );

    reset(eventPublisher); // damit wir nur das Open-Event zählen

    commandService.openVoting(3);

    Voting v = votingRepo.findById(3)
            .orElseThrow(() -> new AssertionError("Voting nicht gefunden"));
    assertTrue(v.isVotingStatus());

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    Object event = eventCaptor.getValue();
    assertEquals("VotingOpenedEvent", event.getClass().getSimpleName());

    // optional: Zeitpunkt prüfen (wenn Event Feld "openedAt"/ähnlich hat, sonst weglassen)
    // Beispiel: Instant.now(fixedClock) – abhängig von deinem Event
    // assertEquals(Instant.now(fixedClock), ...);
  }

  @Test
  void openVoting_unknownId_throwsException() {
    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> commandService.openVoting(999));
    assertTrue(ex.getMessage().contains("nicht gefunden"));
  }

  @Test
  void closeVoting_setsStatusToFalse_andPublishesClosedEvent() {
    commandService.createVoting(
            10,
            "Abstimmung Close",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    commandService.openVoting(10);

    reset(eventPublisher);

    commandService.closeVoting(10);

    Voting v = votingRepo.findById(10).orElseThrow();
    assertFalse(v.isVotingStatus());

    ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertEquals("VotingClosedEvent", eventCaptor.getValue().getClass().getSimpleName());
  }

  @Test
  void closeVoting_unknownId_throwsException() {
    IllegalArgumentException ex =
            assertThrows(IllegalArgumentException.class, () -> commandService.closeVoting(999));
    assertTrue(ex.getMessage().contains("nicht gefunden"));
  }
}
