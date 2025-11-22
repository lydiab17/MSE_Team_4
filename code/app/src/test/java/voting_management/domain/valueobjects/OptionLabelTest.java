package voting_management.domain.valueobjects;

import com.evote.app.votingmanagement.domain.valueobjects.OptionLabel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class OptionLabelTest {

    // ---------- Happy Path ----------

    @Test
    @DisplayName("Gültige Option: einfacher Text")
    void valid_simpleText() {
        OptionLabel label = new OptionLabel("Ja");
        assertEquals("Ja", label.getValue());
    }

    @Test
    @DisplayName("Gültige Option: Buchstaben, Ziffern und Leerzeichen erlaubt")
    void valid_lettersDigitsSpaces() {
        OptionLabel label = new OptionLabel("Option 1A 2025");
        assertEquals("Option 1A 2025", label.getValue());
    }

    @Test
    @DisplayName("Gültige Option: führende und nachfolgende Leerzeichen werden getrimmt")
    void valid_trimmedValue() {
        OptionLabel label = new OptionLabel("   Ja   ");
        assertEquals("Ja", label.getValue(), "Value sollte getrimmt werden");
    }

    // ---------- Negative: null / leer / nur Spaces ----------

    @Test
    @DisplayName("Null-Wert: wirft IllegalArgumentException")
    void nullValue_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> new OptionLabel(null));
    }

    @ParameterizedTest(name = "Ungültige leere Option: \"{0}\"")
    @ValueSource(strings = {
            "",
            "   "   // nur Leerzeichen
    })
    @DisplayName("Leere und nur aus Leerzeichen bestehende Strings sind nicht erlaubt")
    void emptyOrBlank_throwsException(String raw) {
        assertThrows(IllegalArgumentException.class,
                () -> new OptionLabel(raw));
    }

    // ---------- Negative: unerlaubte Zeichen ----------

    @ParameterizedTest(name = "Ungültiges Sonderzeichen in Option: \"{0}\"")
    @ValueSource(strings = {
            "Ja!",
            "Nein?",
            "Vielleicht.",
            "Option_1",
            "Option-1",
            "Ja/Nein",
            "Ja, Nein"
    })
    @DisplayName("Sonderzeichen wie ! ? . , - _ / sind nicht erlaubt")
    void invalidCharacters_throwsException(String raw) {
        assertThrows(IllegalArgumentException.class,
                () -> new OptionLabel(raw));
    }

    // ---------- Unicode / Umlaute ----------

    @Test
    @DisplayName("Umlaute und andere Buchstaben aus Unicode sind erlaubt")
    void umlauts_allowed() {
        OptionLabel label = new OptionLabel("ÄÖÜ äöü ß");
        assertEquals("ÄÖÜ äöü ß", label.getValue());
    }
}
