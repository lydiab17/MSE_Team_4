package voting_management.domain.valueobjects;

import com.evote.app.votingmanagement.domain.valueobjects.VotingName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class VotingNameTest {

    // ---------- Happy Path ----------

    @Test
    @DisplayName("Gültiger Name: genau 10 Zeichen, beginnt mit Großbuchstaben")
    void valid_exactMinLength() {
        String name = "Abstimmung"; // 10 Zeichen
        assertEquals(10, name.trim().length());

        VotingName votingName = new VotingName(name);

        assertEquals(name, votingName.getValue());
    }

    @Test
    @DisplayName("Gültiger Name: innerhalb der Länge 10–100, Großbuchstabe und erlaubte Zeichen")
    void valid_normalName() {
        String name = "Abstimmung 2030 A1";
        assertTrue(name.trim().length() >= 10 && name.trim().length() <= 100);

        VotingName votingName = new VotingName(name);

        assertEquals(name, votingName.getValue());
    }

    @Test
    @DisplayName("Gültiger Name: Umlaute sind erlaubt und erster Buchstabe groß")
    void valid_withUmlauts() {
        String name = "Ämterwahl 2035";
        VotingName votingName = new VotingName(name);

        assertEquals(name, votingName.getValue());
    }

    @Test
    @DisplayName("Gültiger Name: führende/trailing Spaces werden getrimmt")
    void valid_trimmed() {
        String raw = "   Abstimmung XYZ   ";
        String trimmed = raw.trim();
        assertTrue(trimmed.length() >= 10);

        VotingName votingName = new VotingName(raw);

        assertEquals(trimmed, votingName.getValue(), "Value sollte getrimmt werden");
    }

    // ---------- Negative: null / leer / nur Spaces ----------

    @Test
    @DisplayName("Null-Wert: wirft IllegalArgumentException")
    void nullValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(null));
    }

    @ParameterizedTest(name = "Ungültiger leerer Name: \"{0}\"")
    @ValueSource(strings = {
            "",
            "   "   // nur Leerzeichen
    })
    @DisplayName("Leere und nur aus Leerzeichen bestehende Strings sind nicht erlaubt")
    void emptyOrBlank_throwsException(String raw) {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(raw));
    }

    // ---------- Negative: Länge ----------

    @Test
    @DisplayName("Zu kurz: weniger als 10 Zeichen → Exception")
    void tooShort_throwsException() {
        String name = "KurzName"; // < 10
        assertTrue(name.trim().length() < 10);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(name));
    }

    @Test
    @DisplayName("Zu lang: mehr als 100 Zeichen → Exception")
    void tooLong_throwsException() {
        // baue 101 Zeichen: 'A' + 100 x 'b'
        StringBuilder sb = new StringBuilder();
        sb.append('A');
        for (int i = 0; i < 100; i++) {
            sb.append('b');
        }
        String name = sb.toString();
        assertTrue(name.trim().length() > 100);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(name));
    }

    // ---------- Negative: erster Buchstabe klein ----------

    @Test
    @DisplayName("Erster Buchstabe klein (nach Trim) → Exception")
    void firstCharLowercase_throwsException() {
        String name = "abstimmung 2030"; // beginnt klein, Länge ok
        assertTrue(name.trim().length() >= 10);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(name));
    }

    @Test
    @DisplayName("Erster sichtbarer Buchstabe nach Spaces ist klein → Exception")
    void firstCharAfterSpacesLowercase_throwsException() {
        String name = "   abstimmung mit Spaces";
        assertTrue(name.trim().length() >= 10);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(name));
    }

    // ---------- Negative: unerlaubte Zeichen ----------

    @ParameterizedTest(name = "Ungültiger Name mit Sonderzeichen: \"{0}\"")
    @ValueSource(strings = {
            "Abstimmung!",   // !
            "Abstimmung?",   // ?
            "Abstimmung.",   // .
            "Abstimmung,",   // ,
            "Abstimmung-1",  // -
            "Abstimmung_1",  // _
            "Abstimmung/1"   // /
    })
    @DisplayName("Sonderzeichen wie ! ? . , - _ / sind nicht erlaubt")
    void invalidCharacters_throwsException(String raw) {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingName(raw));
    }
}
