package citizen_management.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import com.evote.app.citizen_management.domain.valueobjects.Adress;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testklasse für den Adress-Record.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@DisplayName("Adress Value Objects Tests")
class AdressTest {

    // Happy-Path-Tests


    // Edge-Cases


    // Negative Tests
    @Test
    @DisplayName("Sollte bei leerer Straße Exception werfen")
    void shouldThrowExceptionForEmptyStreet() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("", "12345", "Musterstadt"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress(" ", "12345", "Musterstadt"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress(null, "12345", "Musterstadt"));
    }

    @Test
    @DisplayName("Sollte bei leerer PLZ Exception werfen")
    void shouldThrowExceptionForEmptyZipCode() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", "", "Musterstadt"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", " ", "Musterstadt"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", null, "Musterstadt"));
    }

    @Test
    @DisplayName("Sollte bei leerer Stadt Exception werfen")
    void shouldThrowExceptionForEmptyCity() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", "12345", ""));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", "12345", " "));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße 1", "12345", null));
    }
}
