package voting_management.interfaces.rest;

import com.evote.app.votingmanagement.application.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.interfaces.rest.VotingRestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VotingRestControllerTest {

    @Mock
    private VotingApplicationService service;

    @InjectMocks
    private VotingRestController controller;

    // Helper für gültige Votings
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
    @DisplayName("create: ruft ApplicationService korrekt auf und mappt Response")
    void create_callsServiceAndMapsResponse() {
        LocalDate start = LocalDate.of(2030, 5, 10);
        LocalDate end = start.plusDays(7);

        // Das hier ist der simulierte Body im POST Request
        VotingRestController.CreateVotingRequest request =
                new VotingRestController.CreateVotingRequest(
                        1,
                        "Abstimmung 1",
                        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                        start,
                        end,
                        List.of("Ja", "Nein")
                );

        // Hieraus wird ein Voting Objekt erstellt
        Voting voting = createValidVoting(1);

        // Das hier bedeutet: Wenn die Methode createVoting vom Mock Objekt aufgerufen wird
        // mit den Parametern irgendein int, dann irgendein string ...
        // Soll immer das voting Objekt zurückgegeben werden
        when(service.createVoting(
                anyInt(),
                anyString(),
                anyString(),
                any(LocalDate.class),
                any(LocalDate.class),
                anySet()
        )).thenReturn(voting);

        // Hier wird die eigentliche Methode erst aufgerufen.
        // Der Controller ruft dann ja die service.createVoting auf, die dann einfach voting zurückgibt
        VotingRestController.VotingResponse response = controller.create(request);

        // Hier wird geprüft, ob die Rückgabe im Controller korrekt ist
        assertEquals(1, response.id());
        assertEquals("Abstimmung 1", response.name());
        assertEquals(voting.getInfo(), response.info());
        assertEquals(start, response.startDate());
        assertEquals(end, response.endDate());
        assertFalse(response.open()); // votingStatus ist initial false

        // Hier wird geprüft, ob der Controller die service.createVoting mit den richtigen
        // Parametern aufgerufen hat.
        verify(service).createVoting(
                eq(1),
                eq(request.name()),
                eq(request.info()),
                eq(request.startDate()),
                eq(request.endDate()),
                anySet()
        );
    }

    // ---------- open(...) ----------

    @Test
    @DisplayName("open: delegiert an ApplicationService.openVoting")
    void open_delegatesToService() {
        controller.open(5);
        verify(service).openVoting(5);
    }

    // ---------- getById(...) ----------

    @Test
    @DisplayName("getById: Voting gefunden → liefert VotingResponse")
    void getById_found_returnsResponse() {
        Voting voting = createValidVoting(2);
        // Optional vermeidet Null Pointer E, wenn nichts gefunden wird
        when(service.getVotingById(2)).thenReturn(Optional.of(voting));

        // Hier findet der eigentliche Aufruf statt
        VotingRestController.VotingResponse response = controller.getById(2);

        // Prüfung ob Ausgabe mit Eingabe übereinstimmt
        assertEquals(2, response.id());
        assertEquals(voting.getName(), response.name());
        assertEquals(voting.getInfo(), response.info());
        assertEquals(voting.getStartDate(), response.startDate());
        assertEquals(voting.getEndDate(), response.endDate());
        assertEquals(voting.isVotingStatus(), response.open());
    }

    @Test
    @DisplayName("getById: Voting nicht gefunden → wirft IllegalArgumentException")
    void getById_notFound_throwsException() {
        // Wenn mit dem Parameter aufgerufen wird, soll es nichts finden
        when(service.getVotingById(99)).thenReturn(Optional.empty());

        // Erwartet vom Controller dann eine IllegalArgumentException
        assertThrows(IllegalArgumentException.class,
                () -> controller.getById(99));
    }

    // ---------- getOpen() ----------

    @Test
    @DisplayName("getOpen: mappt Liste der Domain-Votings zu VotingResponses")
    void getOpen_mapsDomainListToResponses() {
        Voting v1 = createValidVoting(1);
        v1.setVotingStatus(true);
        Voting v2 = createValidVoting(2);
        v2.setVotingStatus(true);

        when(service.getOpenVotings(any()))
                .thenReturn(List.of(v1, v2));

        List<VotingRestController.VotingResponse> responses = controller.getOpen();

        assertEquals(2, responses.size());
        assertEquals(1, responses.get(0).id());
        assertEquals(2, responses.get(1).id());

        verify(service).getOpenVotings(any());
    }
}
