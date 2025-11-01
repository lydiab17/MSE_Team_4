package io.github.lydiab17.mseteam4;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
        String name = "Abstimmng"; // 10 Zeichen? -> A b s t i m m n g (9) – also lieber explizit bauen:
        name = "Abstimmungen"; // 12 – machen wir es exakt:
        name = "Abstimmung"; // 10 Zeichen
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
        String info = "Beschreibung hat genau 30 Zeichen"; // zähle: 31? Wir bauen sicher:
        info = "AbcdefghijAbcdefghijAbcdefghij"; // 30 exakt
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
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(10, badName,
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("Ja", "Nein")));
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
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(11, "Abstimmung OK",
                        badInfo, today, today.plusDays(1),
                        opts("Ja", "Nein")));
    }

    // ---------- Negative: Optionen ----------
    @Test
    @DisplayName("Optionen: weniger als 2")
    void options_tooFew() {
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(12, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("NurEine")));
    }

    @Test
    @DisplayName("Optionen: mehr als 10")
    void options_tooMany() {
        Set<String> tooMany = opts("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K");
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(13, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        tooMany));
    }

    @Test
    @DisplayName("Optionen: Duplikate (case-insensitive) nicht erlaubt")
    void options_duplicatesNotAllowed() {
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(14, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("Ja", "ja")));
    }

    @Test
    @DisplayName("Optionen: Sonderzeichen nicht erlaubt / leere Option nicht erlaubt")
    void options_invalidToken() {
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(15, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("Ja", "Nein!"))); // Sonderzeichen

        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(16, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("Ja", ""))); // leer
    }

    // ---------- Negative: Datumslogik ----------
    @Test
    @DisplayName("endDate vor startDate → Fehler")
    void dates_invalidOrder() {
        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(17, "Abstimmung OK",
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today.plusDays(5), today.plusDays(1),
                        opts("A", "B")));
    }

    // ---------- isOpen-Logik ----------
    @Test
    @DisplayName("isOpen: bleibt false, wenn votingStatus=false (trotz gültiger Zeit)")
    void isOpen_requiresStatusTrue() {
        Voting v = Voting.create(
                18, "Abstimmung OK",
                "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                today.minusDays(1), today.plusDays(1),
                opts("A", "B")
        );
        assertFalse(v.isOpen(fixedClock));
        v.setVotingStatus(true);
        assertTrue(v.isOpen(fixedClock));
    }

    // ---------- Obergrenzen ----------
    @Test
    @DisplayName("Obergrenze Name (101) → Fehler; Info (1001) → Fehler")
    void maxLengths_enforced() {
        String longName = "A" + repeat('b', 100); // 101
        String longInfo = "L" + repeat('x', 1000); // 1001

        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(19, longName,
                        "Beschreibung Lang Genug Und Mit Großbuchstaben Am Anfang.",
                        today, today.plusDays(1),
                        opts("A", "B")));

        assertThrows(IllegalArgumentException.class, () ->
                Voting.create(20, "Abstimmung OK",
                        longInfo,
                        today, today.plusDays(1),
                        opts("A", "B")));
    }

}

/* Dokumentation

Ich hatte der KI folgendes gesagt:

Es geht darum eine Abstimmung zu erstellen (für digitale Bürgerabstimmungen). Abstimmungsnamen (name) müssen immer
 mit einem Großbuchstaben anfangen und der Titel muss mindestens 10 Zeichen lang sein. es dürfen keine Sonderzeichen
im Titel einer Abstimmung enthalten sein. Die Beschreibung (info) muss mindestens 30 Zeichen lang sein. Diese muss
ebenfalls mit einem Großbuchstaben anfangen. Du sollst also einen Test generieren fürs erstellen einer Abstimmung.
Denk dir ruhig noch weitere Einschränkungen aus die für eine Abstimmung gelten muss damit du die verschiedenen
Test-Cases die in der Aufgabe beschrieben sind durchführen kannst. Es können auch ruhig neue Variablen für die
Klasse vorausgesetzt werden, aber nicht übertreiben. Wenn du Ideen für weitere Methoden hast gerne her damit
(aber auch nicht übertreiben) und nicht die methode abstimmen (höchstens abstimmung noch offen oder sowas da
sich ein anderer Student aus meiner Gruppe um das abstimmen selbst kümmert)

Ich habe mir die Tests angeschaut und die machen in meinen Augen auch alle Sinn.
Angepasst habe ich überhaupt nichts. Ich finde es bei den Edge-Cases ein bisschen komisch das ChatGPT die Beschreibung
 des Tests zuerst in der info Variable erstellt hat und die info Variable dann in der nächsten Zeile mit dem
 tatsächlichen Wert überschrieben hat wie in Zeile 96. Aber kann ich gut mit Leben. Folgend einmal dargestellt:

 void edge_infoExactlyMinLength() {
        String info = "Beschreibung hat genau 30 Zeichen"; // zähle: 31? Wir bauen sicher:
        info = "AbcdefghijAbcdefghijAbcdefghij"; // 30 exakt
	... */

