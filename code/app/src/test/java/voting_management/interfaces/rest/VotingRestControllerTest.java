package voting_management.interfaces.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.interfaces.dto.CastVoteRequest;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.OptionResultResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.evote.app.votingmanagement.interfaces.rest.VotingRestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VotingRestControllerTest {

  @Mock
  private VotingApplicationService service;

  @InjectMocks
  private VotingRestController controller;

  private Voting createValidVoting(int id) {
    LocalDate start = LocalDate.of(2030, 5, 10);
    LocalDate end = start.plusDays(7);
    return Voting.create(
            id,
            "Abstimmung " + id,
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            start,
            end,
            Set.of("Ja", "Nein")
    );
  }

  // ---------- create(...) ----------

  @Test
  @DisplayName("create: ruft Service korrekt auf und mappt Response")
  void create_callsServiceAndMapsResponse() {
    LocalDate start = LocalDate.of(2030, 5, 10);
    LocalDate end = start.plusDays(7);

    CreateVotingRequest request = new CreateVotingRequest(
            1,
            "Abstimmung 1",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            start,
            end,
            List.of("Ja", "Nein")
    );

    Voting voting = createValidVoting(1);

    when(service.createVoting(
            anyInt(),
            anyString(),
            anyString(),
            any(LocalDate.class),
            any(LocalDate.class),
            anySet()
    )).thenReturn(voting);

    VotingResponse response = controller.create(request);

    assertEquals(1, response.id());
    assertEquals("Abstimmung 1", response.name());
    assertEquals(voting.getInfo(), response.info());
    assertEquals(start, response.startDate());
    assertEquals(end, response.endDate());
    assertEquals(voting.isVotingStatus(), response.open());
    assertEquals(voting.getOptionTexts(), response.options());

    verify(service).createVoting(
            eq(1),
            eq(request.name()),
            eq(request.info()),
            eq(request.startDate()),
            eq(request.endDate()),
            anySet()
    );
  }

  @Test
  @DisplayName("create: Options werden in LinkedHashSet konvertiert (Duplikate raus, Reihenfolge bleibt)")
  void create_convertsOptionsToLinkedHashSet_dedupes_preservesOrder() {
    LocalDate start = LocalDate.of(2030, 5, 10);
    LocalDate end = start.plusDays(7);

    CreateVotingRequest request = new CreateVotingRequest(
            2,
            "Abstimmung 2",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            start,
            end,
            List.of("Ja", "Nein", "Ja")
    );

    Voting voting = createValidVoting(2);

    when(service.createVoting(anyInt(), anyString(), anyString(), any(LocalDate.class), any(LocalDate.class), anySet()))
            .thenReturn(voting);

    controller.create(request);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Set<String>> optionsCaptor = ArgumentCaptor.forClass(Set.class);

    verify(service).createVoting(
            eq(2),
            eq(request.name()),
            eq(request.info()),
            eq(request.startDate()),
            eq(request.endDate()),
            optionsCaptor.capture()
    );

    Set<String> passed = optionsCaptor.getValue();
    assertNotNull(passed);
    assertTrue(passed instanceof LinkedHashSet, "Controller sollte LinkedHashSet verwenden");
    assertEquals(List.of("Ja", "Nein"), passed.stream().toList());
  }

  // ---------- open(...) ----------

  @Test
  @DisplayName("open: delegiert an service.openVoting")
  void open_delegatesToService() {
    controller.open(5);
    verify(service).openVoting(5);
  }

  // ---------- getById(...) ----------

  @Test
  @DisplayName("getById: Voting gefunden -> Response")
  void getById_found_returnsResponse() {
    Voting voting = createValidVoting(2);
    when(service.getVotingById(2)).thenReturn(Optional.of(voting));

    VotingResponse response = controller.getById(2);

    assertEquals(2, response.id());
    assertEquals(voting.getName(), response.name());
    assertEquals(voting.getInfo(), response.info());
    assertEquals(voting.getStartDate(), response.startDate());
    assertEquals(voting.getEndDate(), response.endDate());
    assertEquals(voting.isVotingStatus(), response.open());
    assertEquals(voting.getOptionTexts(), response.options());
  }

  @Test
  @DisplayName("getById: Voting nicht gefunden -> IllegalArgumentException")
  void getById_notFound_throwsException() {
    when(service.getVotingById(99)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> controller.getById(99));
    assertEquals("Voting nicht gefunden", ex.getMessage());
  }

  // ---------- getOpen() ----------

  @Test
  @DisplayName("getOpen: mappt offene Votings zu Response-Liste")
  void getOpen_mapsDomainListToResponses() {
    Voting v1 = createValidVoting(1);
    v1.setVotingStatus(true);
    Voting v2 = createValidVoting(2);
    v2.setVotingStatus(true);

    when(service.getOpenVotings(any()))
            .thenReturn(List.of(v1, v2));

    List<VotingResponse> responses = controller.getOpen();

    assertEquals(2, responses.size());
    assertEquals(1, responses.get(0).id());
    assertEquals(2, responses.get(1).id());

    verify(service).getOpenVotings(any());
  }

  // ---------- getNotOpen() ----------

  @Test
  @DisplayName("getNotOpen: mappt nicht-offene Votings zu Response-Liste")
  void getNotOpen_mapsDomainListToResponses() {
    Voting v1 = createValidVoting(10);
    v1.setVotingStatus(false);
    Voting v2 = createValidVoting(11);
    v2.setVotingStatus(false);

    when(service.getNotOpenVotings()).thenReturn(List.of(v1, v2));

    List<VotingResponse> responses = controller.getNotOpen();

    assertEquals(2, responses.size());
    assertEquals(10, responses.get(0).id());
    assertEquals(11, responses.get(1).id());
    assertFalse(responses.get(0).open());
    assertFalse(responses.get(1).open());

    verify(service).getNotOpenVotings();
  }

  // ---------- castVote(...) ----------

  @Test
  @DisplayName("castVote: extrahiert Token aus 'Bearer <jwt>' und delegiert an service.castVote")
  void castVote_stripsBearerPrefix_andCallsService() {
    CastVoteRequest request = new CastVoteRequest("Ja");

    controller.castVote(7, request, "Bearer jwt-abc");

    ArgumentCaptor<CastVoteDto> captor = ArgumentCaptor.forClass(CastVoteDto.class);
    verify(service).castVote(captor.capture());

    CastVoteDto dto = captor.getValue();
    assertEquals("jwt-abc", dto.authToken);
    assertEquals(7, dto.votingId);
    assertEquals("Ja", dto.optionId);
  }

  @Test
  @DisplayName("castVote: wenn Authorization kein 'Bearer ' hat, wird Header unverändert genutzt")
  void castVote_withoutBearer_usesHeaderAsToken() {
    CastVoteRequest request = new CastVoteRequest("Nein");

    controller.castVote(8, request, "raw-token");

    ArgumentCaptor<CastVoteDto> captor = ArgumentCaptor.forClass(CastVoteDto.class);
    verify(service).castVote(captor.capture());

    CastVoteDto dto = captor.getValue();
    assertEquals("raw-token", dto.authToken);
    assertEquals(8, dto.votingId);
    assertEquals("Nein", dto.optionId);
  }

  // ---------- getResults(...) ----------

  @Test
  @DisplayName("getResults: mappt OptionResult-Liste zu VotingResultsResponse")
  void getResults_mapsOptionResults() {
    when(service.getResultsForVoting(3)).thenReturn(List.of(
            new OptionResult("Ja", 10),
            new OptionResult("Nein", 3)
    ));

    VotingResultsResponse resp = controller.getResults(3);

    assertEquals(3, resp.votingId());
    assertEquals(2, resp.results().size());

    OptionResultResponse r0 = resp.results().get(0);
    OptionResultResponse r1 = resp.results().get(1);

    assertEquals("Ja", r0.option());
    assertEquals(10L, r0.count());

    assertEquals("Nein", r1.option());
    assertEquals(3L, r1.count());

    verify(service).getResultsForVoting(3);
  }
}
