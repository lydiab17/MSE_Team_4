package citizen_management.domain.valueobjects;

import com.evote.app.citizen_management.domain.valueobjects.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testklasse für das Passwort-Record.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@DisplayName("Name Value Objects Tests")
public class PasswordTest {

    // Happy-Path-Tests
    @Test
    @DisplayName("Sollte ein gültiges Passwort akzeptieren")
    void shouldAcceptValidPassword() {
        assertDoesNotThrow(() -> new Password("Passwort123"));
        // Passwort ist gültig: mindestens 8 Zeichen, enthält Buchstaben und Zahlen
    }

    // Edge-Cases

    // Negative Tests
    @Test
    @DisplayName("Sollte bei zu kurzem Passwort eine Exception werfen")
    void shouldThrowExceptionWhenPasswordTooShort() {
        assertThrows(IllegalArgumentException.class, () -> new Password("12AB"));
        // Passwort mit nur 4 Zeichen
    }

    @Test
    @DisplayName("Sollte eine Exception werfen, wenn kein Buchstabe enthalten ist")
    void shouldThrowExceptionWhenNoLetterPresent() {
        assertThrows(IllegalArgumentException.class, () -> new Password("12345678"));
        // Passwort enthält nur Zahlen (mindestens ein Buchstabe muss vorhanden sein)
    }

    @Test
    @DisplayName("Sollte eine Exception werfen, wenn keine Zahl enthalten ist")
    void shouldThrowExceptionWhenNoDigitPresent() {
        assertThrows(IllegalArgumentException.class, () -> new Password("ABCDEFGH"));
        // Passwort enthält nur Buchstaben (mindestens eine Zahl muss vorhanden sein)
    }
}
