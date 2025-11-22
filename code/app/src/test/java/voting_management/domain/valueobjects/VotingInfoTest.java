package voting_management.domain.valueobjects;

import com.evote.app.votingmanagement.domain.valueobjects.VotingInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class VotingInfoTest {

    // ---------- Happy Path ----------

    @Test
    @DisplayName("Gültige Info: Mindestlänge und Großbuchstabe am Anfang")
    void valid_minLengthAndCapitalStart() {
        // 30 Zeichen, beginnt mit Großbuchstaben
        String text = "AbcdefghijAbcdefghijAbcdefghij"; // 30 Zeichen
        assertEquals(30, text.trim().length());

        VotingInfo info = new VotingInfo(text);

        assertEquals(text, info.getValue());
    }

    @Test
    @DisplayName("Gültige Info: Mehrzeiliger Text mit Zeilenumbruch ist erlaubt")
    void valid_multilineAllowed() {
        String text = "Beschreibung Mit Mehreren Zeilen,\nund noch ein bisschen mehr Text.";
        // nur grob sicherstellen, dass es lang genug ist
        assertTrue(text.trim().length() >= 30);

        VotingInfo info = new VotingInfo(text);

        assertEquals(text, info.getValue());
    }

    @Test
    @DisplayName("Gültige Info: führende/trailing Spaces sind erlaubt, werden aber für die Prüfung getrimmt")
    void valid_withLeadingAndTrailingSpaces() {
        String raw = "   Beschreibung mit genug Länge und Großbuchstaben.   ";
        VotingInfo info = new VotingInfo(raw);

        // getValue() gibt den Original-String zurück
        assertEquals(raw, info.getValue());
    }

    // ---------- Negative: null / leer / nur Spaces ----------

    @Test
    @DisplayName("Null-Wert: wirft IllegalArgumentException")
    void nullValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(null));
    }

    @ParameterizedTest(name = "Ungültige leere Info: \"{0}\"")
    @ValueSource(strings = {
            "",
            "   "   // nur Leerzeichen
    })
    @DisplayName("Leere und nur aus Leerzeichen bestehende Strings sind nicht erlaubt")
    void emptyOrBlank_throwsException(String raw) {
        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(raw));
    }

    // ---------- Negative: Länge ----------

    @Test
    @DisplayName("Zu kurz: weniger als 30 Zeichen → Exception")
    void tooShort_throwsException() {
        String text = "Zu kurz für Info"; // deutlich < 30
        assertTrue(text.trim().length() < 30);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(text));
    }

    @Test
    @DisplayName("Zu lang: mehr als 1000 Zeichen → Exception")
    void tooLong_throwsException() {
        // baue 1001 Zeichen: 'L' + 1000 x 'x'
        StringBuilder sb = new StringBuilder();
        sb.append('L');
        for (int i = 0; i < 1000; i++) {
            sb.append('x');
        }
        String text = sb.toString();
        assertTrue(text.trim().length() > 1000);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(text));
    }

    // ---------- Negative: erster Buchstabe klein ----------

    @Test
    @DisplayName("Erster Buchstabe klein (nach Trim) → Exception")
    void firstCharLowercase_throwsException() {
        String text = "beschreibung beginnt klein aber ist lang genug........................";
        assertTrue(text.trim().length() >= 30);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(text));
    }

    @Test
    @DisplayName("Erster sichtbarer Buchstabe nach Spaces ist klein → Exception")
    void firstCharAfterSpacesLowercase_throwsException() {
        String text = "   beschreibung mit führenden Spaces und kleinem Anfangsbuchstaben........................";
        assertTrue(text.trim().length() >= 30);

        assertThrows(IllegalArgumentException.class,
                () -> new VotingInfo(text));
    }
}
