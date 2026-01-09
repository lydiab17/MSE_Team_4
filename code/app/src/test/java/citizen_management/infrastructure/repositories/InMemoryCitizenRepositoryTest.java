package citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.infrastructure.repositories.InMemoryCitizenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.evote.app.citizen_management.domain.valueobjects.Name;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryCitizenRepositoryTest {

    private InMemoryCitizenRepository repository;

    @BeforeEach
    void setup() {
        repository = new InMemoryCitizenRepository();
    }

    @Test
    @DisplayName("Sollte einen Citizen speichern und dessen E-Mail danach auffinden")
    void shouldSaveAndFindCitizen() {
        // Value Objects anlegen
        Name name = new Name("Max", "Mustermann");
        Email email = new Email("max@example.com");
        Password password = new Password("SehrSicher123");

        // Citizen über Factory erzeugen
        Citizen citizen = Citizen.create(name, email, password);

        // Citizen-Objekt im Repository speichern
        repository.save(citizen);

        // Citizen anhand der E-Mail
        Optional<Citizen> result = repository.findByEmail(email);

        // Assertions
        // prüft, dass der Citizen tatsächlich gefunden wurde
        assertTrue(result.isPresent());
        // prüft, dass das gespeicherte Objekt mit dem gefundenen Objekt übereinstimmt
        assertEquals(citizen, result.get());
    }

    @Test
    @DisplayName("clear(): Sollte alle gespeicherten Citizens entfernen")
    void clear_shouldRemoveAllCitizens() {
        // Arrange
        Name name = new Name("Max", "Mustermann");
        Email email = new Email("max@example.com");
        Password password = new Password("SehrSicher123");
        Citizen citizen = Citizen.create(name, email, password);

        repository.save(citizen);
        assertTrue(repository.findByEmail(email).isPresent(),
                "Vor clear() sollte Citizen vorhanden sein");

        // Act
        repository.clear();

        // Assert
        Optional<Citizen> result = repository.findByEmail(email);
        assertTrue(result.isEmpty(),
                "Nach clear() sollte kein Citizen mehr vorhanden sein");
    }
}