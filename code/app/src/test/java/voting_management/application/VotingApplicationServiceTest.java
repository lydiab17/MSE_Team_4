package voting_management.application;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.dto.OptionResult;
import com.evote.app.votingmanagement.application.port.AuthPort;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Vote;
import com.evote.app.votingmanagement.domain.model.Voting;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVoteRepository;
import com.evote.app.votingmanagement.infrastructure.repositories.InMemoryVotingRepository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

public class VotingApplicationServiceTest {

  private InMemoryVotingRepository votingRepo;
  private InMemoryVoteRepository voteRepo;
  private CapturingEventPublisher eventPublisher;
  private AuthPort authPort;
  private VotingApplicationService service;
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

    eventPublisher = new CapturingEventPublisher();

    // Default: immer authentifiziert, immer gleiches Pseudonym (für success/double-vote Tests ausreichend)
    authPort = token -> Optional.of(new PseudonymToken("p-1"));

    service = new VotingApplicationService(votingRepo, voteRepo, authPort, eventPublisher, fixedClock);
  }

  // ---------------- Helpers ----------------

  private Set<String> opts(String... vals) {
    Set<String> set = new LinkedHashSet<>();
    Collections.addAll(set, vals);
    return set;
  }

  /**
   * CastVoteDto ist evtl. record oder Klasse mit anderem Konstruktor/Fields.
   * Diese Factory versucht:
   * - record canonical ctor via RecordComponents
   * - ctor mit (int,String,String) oder (String,int,String) etc. heuristisch
   * - no-arg ctor + field-set via reflection
   */
  private CastVoteDto newDto(int votingId, String optionId, String authToken) {
    try {
      Class<?> clazz = CastVoteDto.class;

      // 1) record?
      if (clazz.isRecord()) {
        var comps = clazz.getRecordComponents();
        Class<?>[] types = Arrays.stream(comps).map(rc -> rc.getType()).toArray(Class<?>[]::new);
        Object[] args = new Object[comps.length];

        for (int i = 0; i < comps.length; i++) {
          String n = comps[i].getName().toLowerCase(Locale.ROOT);
          if (n.contains("voting") && n.contains("id")) args[i] = votingId;
          else if (n.contains("option")) args[i] = optionId;
          else if (n.contains("auth")) args[i] = authToken;
          else args[i] = null;
        }

        Constructor<?> ctor = clazz.getDeclaredConstructor(types);
        ctor.setAccessible(true);
        return (CastVoteDto) ctor.newInstance(args);
      }

      // 2) ctor heuristik
      for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
        ctor.setAccessible(true);
        Class<?>[] p = ctor.getParameterTypes();
        if (p.length == 3) {
          Object[] args = new Object[3];

          // Positionen anhand Typen füllen (ein int + zwei Strings)
          for (int i = 0; i < 3; i++) {
            if (p[i] == int.class || p[i] == Integer.class) args[i] = votingId;
          }

          // verbleibende String-Slots befüllen: optionId, authToken
          List<Integer> strIdx = new ArrayList<>();
          for (int i = 0; i < 3; i++) {
            if (p[i] == String.class) strIdx.add(i);
          }
          if (strIdx.size() == 2) {
            args[strIdx.get(0)] = optionId;
            args[strIdx.get(1)] = authToken;
            return (CastVoteDto) ctor.newInstance(args);
          }
        }
      }

      // 3) no-arg + fields
      Constructor<?> noArg = clazz.getDeclaredConstructor();
      noArg.setAccessible(true);
      Object dto = noArg.newInstance();
      setFieldIfPresent(dto, "votingId", votingId);
      setFieldIfPresent(dto, "optionId", optionId);
      setFieldIfPresent(dto, "authToken", authToken);
      return (CastVoteDto) dto;

    } catch (Exception e) {
      throw new RuntimeException("Konnte CastVoteDto nicht erstellen (bitte DTO-Struktur prüfen).", e);
    }
  }

  private void setFieldIfPresent(Object target, String fieldName, Object value) {
    try {
      var f = target.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (NoSuchFieldException ignored) {
      // ok
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Object getProperty(Object obj, String... candidates) {
    for (String name : candidates) {
      // 1) method name() (record accessor)
      try {
        Method m = obj.getClass().getMethod(name);
        m.setAccessible(true);
        return m.invoke(obj);
      } catch (Exception ignored) {
      }

      // 2) getName()
      try {
        String getter = "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
        Method m = obj.getClass().getMethod(getter);
        m.setAccessible(true);
        return m.invoke(obj);
      } catch (Exception ignored) {
      }

      // 3) field
      try {
        var f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
      } catch (Exception ignored) {
      }
    }
    throw new AssertionError("Property nicht gefunden: " + Arrays.toString(candidates)
            + " in " + obj.getClass().getName());
  }

  // ---------------- Capturing Publisher ----------------

  static class CapturingEventPublisher implements ApplicationEventPublisher {
    private final List<Object> events = new ArrayList<>();

    @Override
    public void publishEvent(Object event) {
      events.add(event);
    }

    public List<Object> events() {
      return events;
    }

    public Object lastEvent() {
      if (events.isEmpty()) return null;
      return events.get(events.size() - 1);
    }
  }

  // ---------------- Tests: createVoting ----------------

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
                    "zuKurz",
                    "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                    today,
                    today.plusDays(1),
                    opts("Ja", "Nein")
            )
    );
  }

  // ---------------- Tests: openVoting ----------------

  @Test
  void openVoting_changesStatusToTrue_andPublishesEvent() {
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

    assertEquals(1, eventPublisher.events().size(), "openVoting sollte genau ein Event publishen");
    Object event = eventPublisher.lastEvent();
    assertNotNull(event);

    // Keine harte Bindung an API von VotingOpenedEvent: reflection
    assertEquals("VotingOpenedEvent", event.getClass().getSimpleName());

    Object id = getProperty(event, "votingId", "id");
    Object openedAt = getProperty(event, "openedAt", "timestamp", "occurredAt");

    assertEquals(3, ((Number) id).intValue());
    assertEquals(Instant.now(fixedClock), openedAt);
  }

  @Test
  void openVoting_unknownId_throwsException() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.openVoting(999));
    assertTrue(ex.getMessage().contains("nicht gefunden"));
  }

  // ---------------- Tests: closeVoting ----------------

  @Test
  void closeVoting_setsStatusToFalse() {
    service.createVoting(
            10,
            "Abstimmung Close",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    service.openVoting(10);
    assertTrue(votingRepo.findById(10).orElseThrow().isVotingStatus());

    service.closeVoting(10);

    Voting v = votingRepo.findById(10).orElseThrow();
    assertFalse(v.isVotingStatus(), "Voting-Status sollte nach closeVoting false sein");
  }

  @Test
  void closeVoting_unknownId_throwsException() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.closeVoting(999));
    assertTrue(ex.getMessage().contains("nicht gefunden"));
  }

  // ---------------- Tests: getVotingById ----------------

  @Test
  void getVotingById_returnsEmpty_whenNotFound() {
    assertTrue(service.getVotingById(12345).isEmpty());
  }

  @Test
  void getVotingById_returnsVoting_whenFound() {
    service.createVoting(
            11,
            "Abstimmung GetById",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today,
            today.plusDays(5),
            opts("Ja", "Nein")
    );

    var opt = service.getVotingById(11);
    assertTrue(opt.isPresent());
    assertEquals(11, opt.get().getId());
  }

  // ---------------- Tests: getOpenVotings / getNotOpenVotings ----------------

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
    votingRepo.save(v2);

    var openVotings = service.getOpenVotings(fixedClock);

    assertEquals(1, openVotings.size());
    assertEquals(4, openVotings.get(0).getId());
  }

  @Test
  void getOpenVotings_excludesVotingsOutsideDateRange_evenIfStatusTrue() {
    Voting future = service.createVoting(
            6,
            "Abstimmung Zukunft",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.plusDays(5),
            today.plusDays(10),
            opts("Ja", "Nein")
    );
    future.setVotingStatus(true);
    votingRepo.save(future);

    var open = service.getOpenVotings(fixedClock);
    assertTrue(open.isEmpty(), "Außerhalb des Datumsbereichs sollte nicht als offen gelten");
  }

  @Test
  void getNotOpenVotings_returnsOnlyThoseWithStatusFalse() {
    Voting open = service.createVoting(
            7,
            "Abstimmung Offen 2",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    open.setVotingStatus(true);
    votingRepo.save(open);

    Voting closed = service.createVoting(
            8,
            "Abstimmung Zu",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(100),
            today.plusDays(100),
            opts("Ja", "Nein")
    );
    closed.setVotingStatus(false);
    votingRepo.save(closed);

    var notOpen = service.getNotOpenVotings();
    assertEquals(1, notOpen.size());
    assertEquals(8, notOpen.get(0).getId());
  }

  // ---------------- Tests: getResultsForVoting ----------------

  @Test
  void getResultsForVoting_countsVotesPerOption_caseInsensitive_andIncludesZeroCounts() {
    service.createVoting(
            20,
            "Abstimmung Results",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein", "Enthaltung")
    );

    // Votes direkt über Repo (wir testen Ergebnis-Use-Case, nicht castVote hier)
    voteRepo.save(Vote.createNew(20, "Ja", "p1"));
    voteRepo.save(Vote.createNew(20, "ja", "p2")); // case-insensitive zählen
    voteRepo.save(Vote.createNew(20, "Nein", "p3"));
    voteRepo.save(Vote.createNew(999, "Ja", "otherVoting")); // anderes Voting -> darf nicht zählen

    List<OptionResult> results = service.getResultsForVoting(20);
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
    assertThrows(IllegalArgumentException.class, () -> service.getResultsForVoting(404));
  }

  // ---------------- Tests: castVote ----------------

  @Test
  void castVote_success_savesVote() {
    service.createVoting(
            30,
            "Abstimmung CastVote",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    service.openVoting(30);

    Voting voting = votingRepo.findById(30).orElseThrow();
    String optionId = firstOptionIdFromVoting(voting);

    service.castVote(new CastVoteDto("token-1", 30, optionId));

    var votes = voteRepo.findByVotingId(30);
    assertEquals(1, votes.size());
    assertTrue(voteRepo.existsByVotingIdAndPseudonym(30, "p-1"));
  }


  @Test
  void castVote_authFails_throwsIllegalState() {
    AuthPort failingAuth = token -> Optional.empty();
    VotingApplicationService svc =
            new VotingApplicationService(votingRepo, voteRepo, failingAuth, eventPublisher, fixedClock);

    svc.createVoting(
            31,
            "Abstimmung AuthFail",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    svc.openVoting(31);

    assertThrows(IllegalStateException.class, () -> svc.castVote(newDto(31, "Ja", "bad-token")));
  }

  @Test
  void castVote_votingNotFound_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> service.castVote(newDto(9999, "Ja", "token-1")));
  }

  @Test
  void castVote_votingNotOpened_throwsIllegalState() {
    service.createVoting(
            32,
            "Abstimmung Closed",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    // nicht geöffnet

    assertThrows(IllegalStateException.class, () -> service.castVote(newDto(32, "Ja", "token-1")));
  }

  @Test
  void castVote_optionNotInVoting_throwsIllegalArgument() {
    service.createVoting(
            33,
            "Abstimmung Option",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    service.openVoting(33);

    assertThrows(IllegalArgumentException.class, () -> service.castVote(newDto(33, "Vielleicht", "token-1")));
  }

  @Test
  void castVote_doubleVote_throwsIllegalState() {
    service.createVoting(
            34,
            "Abstimmung Double",
            "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
            today.minusDays(1),
            today.plusDays(1),
            opts("Ja", "Nein")
    );
    service.openVoting(34);

    Voting voting = votingRepo.findById(34).orElseThrow();
    String optionId = firstOptionIdFromVoting(voting);

    service.castVote(new CastVoteDto("token-1", 34, optionId));
    assertThrows(IllegalStateException.class, () ->
            service.castVote(new CastVoteDto("token-1", 34, optionId))
    );
  }


  private String firstOptionIdFromVoting(Voting voting) {
    // Versuche gängige Getter/Strukturen: getOptions(), options(), getOptionMap() etc.
    try {
      // 1) Methode "getOptions" / "options"
      for (String mName : List.of("getOptions", "options")) {
        try {
          var m = voting.getClass().getMethod(mName);
          Object optionsObj = m.invoke(voting);
          String id = extractFirstOptionId(optionsObj);
          if (id != null) return id;
        } catch (NoSuchMethodException ignored) {
        }
      }

      // 2) Methode "getOptionLabels" / "getOptionSet" (falls so benannt)
      for (String mName : List.of("getOptionLabels", "getOptionSet", "getVotingOptions")) {
        try {
          var m = voting.getClass().getMethod(mName);
          Object optionsObj = m.invoke(voting);
          String id = extractFirstOptionId(optionsObj);
          if (id != null) return id;
        } catch (NoSuchMethodException ignored) {
        }
      }

      // 3) Feld-Fallback
      for (String fName : List.of("options", "votingOptions", "optionMap")) {
        try {
          var f = voting.getClass().getDeclaredField(fName);
          f.setAccessible(true);
          Object optionsObj = f.get(voting);
          String id = extractFirstOptionId(optionsObj);
          if (id != null) return id;
        } catch (NoSuchFieldException ignored) {
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Konnte OptionId nicht aus Voting lesen", e);
    }

    throw new AssertionError("Keine OptionId im Voting gefunden (Option-Struktur anders als erwartet).");
  }

  @SuppressWarnings("unchecked")
  private String extractFirstOptionId(Object optionsObj) {
    if (optionsObj == null) return null;

    // a) Map<id, label> oder Map<label, id>
    if (optionsObj instanceof Map<?, ?> map && !map.isEmpty()) {
      Object firstKey = map.keySet().iterator().next();
      Object firstVal = map.values().iterator().next();
      // Heuristik: wenn key String ist und "id-artig", nimm key, sonst val
      if (firstKey instanceof String sKey) return sKey;
      if (firstVal instanceof String sVal) return sVal;
    }

    // b) Collection von Objekten (Option-Objekte)
    if (optionsObj instanceof Collection<?> col && !col.isEmpty()) {
      Object first = col.iterator().next();
      if (first instanceof String s) return s; // z.B. schon IDs
      // Option-Objekt: versuche id()/getId()
      try {
        var m = first.getClass().getMethod("id");
        Object id = m.invoke(first);
        if (id != null) return id.toString();
      } catch (Exception ignored) {
      }
      try {
        var m = first.getClass().getMethod("getId");
        Object id = m.invoke(first);
        if (id != null) return id.toString();
      } catch (Exception ignored) {
      }
    }

    return null;
  }

}
