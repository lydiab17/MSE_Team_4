package citizen_management.domain.valueobjects;

import com.evote.app.citizen_management.domain.valueobjects.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testklasse für den Name-Record.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@DisplayName("Name Value Objects Tests")
class NameTest {

    // Happy-Path-Tests


    // Edge-Cases


    // Negative Tests
    @Test
    @DisplayName("Sollte bei leerem Vornamen Exception werfen")
    void shouldThrowExceptionForEmptyFirstName() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Name("", "Mustermann"));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Name(" ", "Mustermann"));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Name(null, "Mustermann"));
    }

    @Test
    @DisplayName("Sollte bei leerem Nachnamen Exception werfen")
    void shouldThrowExceptionForEmptyLastName() {
        // leerer String
        assertThrows(IllegalArgumentException.class, () -> new Name("Max", ""));
        // nur Whitespace
        assertThrows(IllegalArgumentException.class, () -> new Name("Max", " "));
        // kein Wert
        assertThrows(IllegalArgumentException.class, () -> new Name("Max", null));
    }
}
