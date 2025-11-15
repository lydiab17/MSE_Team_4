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
        assertThrows(IllegalArgumentException.class, () -> new Adress("", "12", "12345", "Musterstadt"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress(" ", "12", "12345", "Musterstadt"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress(null, "12", "12345", "Musterstadt"));
    }

    @Test
    @DisplayName("Sollte bei leerer Hausnummer Exception werfen")
    void shouldThrowExceptionForEmptyHouseNumber() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "", "12345", "Musterstadt"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", " ", "12345", "Musterstadt"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", null, "12345", "Musterstadt"));
    }

    @Test
    @DisplayName("Sollte bei leerer PLZ Exception werfen")
    void shouldThrowExceptionForEmptyZipCode() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", "", "Musterstadt"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", " ", "Musterstadt"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", null, "Musterstadt"));
    }

    @Test
    @DisplayName("Sollte bei leerer Stadt Exception werfen")
    void shouldThrowExceptionForEmptyCity() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", "12345", ""));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", "12345", " "));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Adress("Musterstraße", "12", "12345",null));
    }
}
