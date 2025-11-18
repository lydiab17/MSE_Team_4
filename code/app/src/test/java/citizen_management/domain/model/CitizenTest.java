package citizen_management.domain.model;

import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import com.evote.app.citizen_management.domain.model.Citizen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für die Citizen-Klasse.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@DisplayName("Citizen Model Tests")
public class CitizenTest {

    private Name name;
    private Email email;
    private Password password;

    @BeforeEach
    void setUp() {
        name = new Name("Max", "Mustermann");
        email = new Email("max.mustermann@example.com");
        password = new Password("StrongPassword123");
    }

    // Happy-Path-Tests

    @Test
    @DisplayName("Sollte gültigen Bürger erstellen")
    void shouldCreateValidCitizen() {

        Citizen citizen = Citizen.create(name, email, password);

        assertNotNull(citizen);
        assertNotNull(citizen.getCitizenID());
        assertEquals(name, citizen.getName());
        assertEquals(email, citizen.getEmail());
        assertEquals(password, citizen.getPassword());
    }

    // Edge-Cases


    // Negative Tests
    @Test
    @DisplayName("Sollte Exception werfen, wenn Name null ist")
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(NullPointerException.class,
                () -> Citizen.create(null, email, password));
    }

    @Test
    @DisplayName("Sollte Exception werfen, wenn Email null ist")
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(NullPointerException.class,
                () -> Citizen.create(name, null, password));
    }

    @Test
    @DisplayName("Sollte Exception werfen, wenn Passwort null ist")
    void shouldThrowExceptionWhenPasswordIsNull() {
        assertThrows(NullPointerException.class,
                () -> Citizen.create(name, email, null));
    }
}
