package citizen_management.domain.valueobjects;

import com.evote.app.citizen_management.domain.valueobjects.Name;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @Test
    @DisplayName("Sollte gültigen Namen erstellen")
    void shouldCreateValidName() {
        Name name = new Name("Max", "Mustermann");
        assertEquals("Max", name.firstName());
        assertEquals("Mustermann", name.lastName());
    }

    // Edge-Cases
    @Test
    @DisplayName("Sollte gültigen Namen erstellen, wenn Vorname genau 3 Zeichen hat")
    void shouldAcceptFirstNameWithExactlyThreeCharacters() {
        Name name = new Name("Max", "Muster");
        // Max = 3 Zeichen (3 Zeichen sind minimal erlaubt)
        assertEquals("Max", name.firstName());
        assertEquals("Muster", name.lastName());

    }

    @Test
    @DisplayName("Sollte gültigen Namen erstellen, wenn Nachname genau 3 Zeichen hat")
    void shouldAcceptLastNameWithExactlyThreeCharacters() {
        Name name = new Name("Anna", "Özi");
        // Özi = 3 Zeichen (3 Zeichen sind minimal erlaubt)
        assertEquals("Anna", name.firstName());
        assertEquals("Özi", name.lastName());
    }


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

    @Test
    @DisplayName("Sollte Ausnahme werfen bei zu langem Vornamen")
    void shouldThrowExceptionWhenFirstNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new Name("Maximiliano", "Muster"));
        // Vorname hat 11 Zeichen; nur 10 Zeichen maximal erlaubt
    }
    @Test
    @DisplayName("Sollte Ausnahme werfen bei zu langem Nachnamen")
    void shouldThrowExceptionWhenLastNameTooLong() {
        assertThrows(IllegalArgumentException.class, () -> new Name("Max", "Mustermanns"));
        // Nachname hat 11 Zeichen; nur 10 Zeichen maximal erlaubt
    }
}
