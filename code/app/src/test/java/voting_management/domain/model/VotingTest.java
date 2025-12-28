package voting_management.domain.model;

import com.evote.app.votingmanagement.domain.model.Voting;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.*;

public class VotingTest {
    private Clock fixedClock;
    private LocalDate today;

    @BeforeEach
    void setup() {
        this.today = LocalDate.of(2030, 5, 10); // fixiertes Datum für reproduzierbare Tests
        this.fixedClock = Clock.fixed(today.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
    }

    // ---------- Helpers ----------
    private Set<String> opts(String... vals) {
        return new LinkedHashSet<>(Arrays.asList(vals));
    }

    private String repeat(char c, int len) {
        char[] arr = new char[len];
        Arrays.fill(arr, c);
        return new String(arr);
    }

    // ---------- Happy Path ----------
    // prüft den idealen Ablauf, also den normalen, erwarteten Fall, in dem alles korrekt eingegeben wird und keine Fehler auftreten.
    @Test
    @DisplayName("Erstellung: gültige Eingaben erzeugen Voting; Status=false; isOpen abhängig von Status")
    void create_valid_minimalHappyPath() {
        Voting v = Voting.create(
                1,
                "Abstimmung 2030", // >=10, Großbuchst., nur erlaubte Zeichen
                "Beschreibung Mit Mindestens Dreißig Zeichen Länge.",
                today, today.plusDays(7),
                opts("Ja", "Nein")
        );

        assertNotNull(v);
        assertFalse(v.isVotingStatus(), "Status startet false"); // stellt sicher das Status false ist
        assertFalse(v.isOpen(fixedClock), "Ohne Freischalten nicht offen");

        v.setVotingStatus(true); // Freischalten (öffnen)
        assertTrue(v.isOpen(fixedClock), "Innerhalb [start,end] inkl. Randtage ist offen");
    }

    @Test
    @DisplayName("Umlaute: Name darf mit Ä/Ö/Ü beginnen")
    void create_nameWithUmlautStart_isValid() {
        Voting v = Voting.create(
                2,
                "Ämterwahl 2035",
                "Eine Beschreibung Die Mit Großbuchstaben Startet Und Lang Genug Ist.",
                today, today.plusDays(1),
                opts("Option 1", "Option 2")
        );
        assertNotNull(v);
    }

    // ---------- Edge Cases ----------
    @Test
    @DisplayName("Grenze: Name genau 10 Zeichen ist gültig")
    void edge_nameExactlyMinLength() {
        String name = "Abstimmung"; // 10 Zeichen? -> A b s t i m m n g (9) – also lieber explizit bauen:
        assertEquals(10, name.length());

        Voting v = Voting.create(
                3,
                name,
                "Info Mit Genau Dreißig Zeichen.....", // prüfen wir gleich unten
                today, today.plusDays(3),
                opts("A", "B")
        );
        assertNotNull(v);
    }

    @Test
    @DisplayName("Grenze: Info genau 30 Zeichen ist gültig")
    void edge_infoExactlyMinLength() {
        String info = "AbcdefghijAbcdefghijAbcdefghij"; // zähle: 30
        assertEquals(30, info.length());

        Voting v = Voting.create(
                4,
                "Abstimmung XX",
                info,
                today, today.plusDays(2),
                opts("Ja", "Nein")
        );
        assertNotNull(v);
    }

    @Test
    @DisplayName("Grenze: 2 und 10 Optionen sind erlaubt")
    void edge_minMaxOptions() {
        Voting v2 = Voting.create(
                5,
                "Abstimmung YY",
                "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                today, today.plusDays(2),
                opts("O1", "O2")
        );
        assertNotNull(v2);

        Set<String> ten = opts("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
        Voting v10 = Voting.create(
                6,
                "Abstimmung ZZ",
                "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                today, today.plusDays(2),
                ten
        );
        assertNotNull(v10);
    }

    @Test
    @DisplayName("isOpen: Randtage zählen als offen (inklusive Intervall)")
    void edge_isOpenOnBounds() {
        Voting v = Voting.create(
                7,
                "Abstimmung Rand",
                "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                today, today, // start == end == today
                opts("Ja", "Nein")
        );
        v.setVotingStatus(true);

        assertTrue(v.isOpen(fixedClock), "now == startDate == endDate → offen");
    }

    // ---------- Negative: Name ----------
    // ParameterizedTest wird mehrfach ausgeführt mit den ganzen werten für badName
    @ParameterizedTest(name = "Ungültiger Name: \"{0}\"")
    @ValueSource(strings = {
            "kleiner Titel",     // beginnt klein
            "Abstimmung!",       // Sonderzeichen
            "Abstimmu",          // 8
            "Abstimmng",         // 9
            "         ",         // nur Spaces
            ""                   // leer
    })
    void invalid_name_cases(String badName) {
      var end = today.plusDays(1);
      var options = opts("Ja", "Nein");
      var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

      assertThrows(IllegalArgumentException.class,
              () -> Voting.create(10, badName, info, today, end, options));
    }

  // ---------- Negative: Info ----------
  @ParameterizedTest(name = "Ungültige Info: \"{0}\"")
  @ValueSource(strings = {
          "zu kurz",                          // < 30
          "klein am Anfang aber lang genug .....................................",
          "          ",                        // blank
          ""                                   // leer
  })
  void invalid_info_cases(String badInfo) {
    var end = today.plusDays(1);
    var options = opts("Ja", "Nein");
    var name = "Abstimmung OK";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(11, name, badInfo, today, end, options));
  }

  // ---------- Negative: Optionen ----------
  @Test
  @DisplayName("Optionen: weniger als 2")
  void options_tooFew() {
    var end = today.plusDays(1);
    var options = opts("NurEine");
    var name = "Abstimmung OK";
    var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(12, name, info, today, end, options));
  }

  @Test
  @DisplayName("Optionen: mehr als 10")
  void options_tooMany() {
    var end = today.plusDays(1);
    Set<String> tooMany = opts("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K");
    var name = "Abstimmung OK";
    var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(13, name, info, today, end, tooMany));
  }

  @Test
  @DisplayName("Optionen: Duplikate (case-insensitive) nicht erlaubt")
  void options_duplicatesNotAllowed() {
    var end = today.plusDays(1);
    var options = opts("Ja", "ja");
    var name = "Abstimmung OK";
    var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(14, name, info, today, end, options));
  }

  @Test
  @DisplayName("Optionen: Sonderzeichen nicht erlaubt / leere Option nicht erlaubt")
  void options_invalidToken() {
    var end = today.plusDays(1);
    var name = "Abstimmung OK";
    var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    var optionsWithSpecialChar = opts("Ja", "Nein!");
    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(15, name, info, today, end, optionsWithSpecialChar)); // Sonderzeichen

    var optionsWithEmpty = opts("Ja", "");
    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(16, name, info, today, end, optionsWithEmpty)); // leer
  }

  // ---------- Negative: Datumslogik ----------
  @Test
  @DisplayName("endDate vor startDate → Fehler")
  void dates_invalidOrder() {
    var start = today.plusDays(5);
    var end = today.plusDays(1);
    var options = opts("A", "B");
    var name = "Abstimmung OK";
    var info = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(17, name, info, start, end, options));
  }

  // ---------- Obergrenzen ----------
  @Test
  @DisplayName("Obergrenze Name (101) → Fehler; Info (1001) → Fehler")
  void maxLengths_enforced() {
    var end = today.plusDays(1);
    var options = opts("A", "B");
    var infoOk = "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.";

    String longName = "A" + repeat('b', 100); // 101
    String longInfo = "L" + repeat('x', 1000); // 1001

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(19, longName, infoOk, today, end, options));

    var nameOk = "Abstimmung OK";
    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(20, nameOk, longInfo, today, end, options));
  }

  // ---------- Additional Tests  ----------
// Negativer Test: Testet, dass Voting.create() keine null-Werte akzeptiert
  @Test
  @DisplayName("Null-Werte: Name, Info, Optionen, Start, End werfen Exception")
  void nullValues_throwException() {
    var end = today.plusDays(1);
    var options = opts("Ja", "Nein");
    var info = "Info OK Mit Mehr Als Dreißig Zeichen.";

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(21, null, info, today, end, options));

    assertThrows(IllegalArgumentException.class,
            () -> Voting.create(22, "Abstimmung OK", null, today, end, options));

    assertThrows(NullPointerException.class,
            () -> Voting.create(23, "Abstimmung OK", info, null, end, options));

    assertThrows(NullPointerException.class,
            () -> Voting.create(24, "Abstimmung OK", info, today, null, options));

    assertThrows(NullPointerException.class,
            () -> Voting.create(25, "Abstimmung OK", info, today, end, null));
  }


}