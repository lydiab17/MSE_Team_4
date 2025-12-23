package voting_management.application;

import com.evote.app.votingmanagement.application.port.AuthPort;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVoteRepository;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVotingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class VotingApplicationServiceTest {

    private InMemoryVotingRepository votingRepo;
    private InMemoryVoteRepository voteRepo;
    private ApplicationEventPublisher eventPublisher;
    private AuthPort authPort;
    private VotingApplicationService service;
    private Clock fixedClock;
    private LocalDate today;

    @BeforeEach
    void setup() {
        votingRepo = new InMemoryVotingRepository();
        voteRepo = new InMemoryVoteRepository();

        service = new VotingApplicationService(votingRepo, voteRepo, authPort, eventPublisher, fixedClock);

        today = LocalDate.of(2030, 5, 10);
        fixedClock = Clock.fixed(
                today.atStartOfDay(ZoneId.of("UTC")).toInstant(),
                ZoneId.of("UTC")
        );
    }

    private Set<String> opts(String... vals) {
        Set<String> set = new LinkedHashSet<>();
        for (String v : vals) {
            set.add(v);
        }
        return set;
    }

    @Test
    void createVoting_savesVotingInRepository() {
        Voting created = service.createVoting(
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
    }

    @Test
    void createVoting_invalidName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createVoting(
                        2,
                        "zuKurz", // ungültig
                        "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                        today,
                        today.plusDays(1),
                        opts("Ja", "Nein")
                )
        );
    }

    @Test
    void openVoting_changesStatusToTrue() {
        service.createVoting(
                3,
                "Abstimmung Status",
                "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                today.minusDays(1),
                today.plusDays(1),
                opts("Ja", "Nein")
        );

        service.openVoting(3);

        Voting v = votingRepo.findById(3)
                .orElseThrow(() -> new AssertionError("Voting nicht gefunden"));
        assertTrue(v.isVotingStatus(), "Voting-Status sollte nach openVoting true sein");
    }

    @Test
    void getOpenVotings_returnsOnlyOpenOnes() {
        // Voting 1: offen (Status true, Datum passt)
        Voting v1 = service.createVoting(
                4,
                "Abstimmung Offen",
                "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                today.minusDays(1),
                today.plusDays(1),
                opts("Ja", "Nein")
        );
        v1.setVotingStatus(true);
        votingRepo.save(v1);

        // Voting 2: zu, weil Status false
        Voting v2 = service.createVoting(
                5,
                "Abstimmung Geschlossen",
                "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                today.minusDays(1),
                today.plusDays(1),
                opts("Ja", "Nein")
        );
        // Status bleibt false
        votingRepo.save(v2);

        var openVotings = service.getOpenVotings(fixedClock);


        assertEquals(1, openVotings.size());
        assertEquals(4, openVotings.get(0).getId());
    }


}
