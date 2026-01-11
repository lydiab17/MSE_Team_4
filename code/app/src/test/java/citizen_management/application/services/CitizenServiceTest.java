package citizen_management.application.services;

import com.evote.app.citizen_management.application.CitizenAggregator;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenProjector;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import com.evote.app.citizen_management.infrastructure.repositories.InMemoryCitizenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { CitizenService.class,
        CitizenAggregator.class,
        CitizenProjector.class,
        EventStore.class,
        InMemoryCitizenRepository.class })
class CitizenServiceTest {


    @Autowired
    private CitizenService citizenService;

    @Autowired
    private CitizenRepository citizenRepository;

    // Diese Methode wird nach JEDEM Test ausgeführt
    // Sie sorgt dafür, dass das Repository geleert wird,
    // damit die Tests sich nicht gegenseitig beeinflussen
    @AfterEach
    void cleanUp() {
        citizenRepository.clear();
    }

    // Happy Path Test
    // Testet, ob die Registrierung eines Bürgers erfolgreich ist
    @Test
    void testRegistrationSuccessful() throws UserAlreadyExistsException {
        // arrange
        // Es wird ein DTO mit gültigen Registrierungsdaten erstellt
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

        // act
        // Aufruf der Methode, die getestet werden soll
        Citizen citizen = citizenService.registerCitizen(citizenInput);

        // assert
        // Überprüfung, ob die gespeicherten Daten korrekt sind
        assertEquals("Max", citizen.getName().firstName());
        assertEquals("Mustermann", citizen.getName().lastName());
        assertEquals("max.mustermann@test.de", citizen.getEmail().email());
    }

    // Negativer Test
    // Testet, ob die Registrierung fehlschlägt, wenn das PW zu kurz ist
    @Test
    void testRegistrationUnsuccessfulShortPW() throws IllegalArgumentException {
        // arrange
        // Erstellung eines DTOs mit einem zu kurzen Passwort
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "ohnepw");

        // act and check
        // Erwartet wird, dass eine IllegalArgumentException geworfen wird
        assertThrows(IllegalArgumentException.class, () -> citizenService.registerCitizen(citizenInput));
    }

    // Happy Path Test
    // Testet, ob der Login mit korrekten Zugangsdaten funktioniert
    @Test
    void testLoginSuccessful() throws UserAlreadyExistsException {
        // arrange
        // Zuerst wird ein Bürger registriert
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
        Citizen citizen = citizenService.registerCitizen(citizenInput);

        // act
        // Versuch, sich mit den korrekten Zugangsdaten einzuloggen
        boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "testtest1234");

        // assert
        // Der Login sollte erfolgreich sein
        assertTrue(loginSuccessful);
    }

    // Negativer Test
    // Testet, ob der Login mit falschem Passwort fehlschlägt
    @Test
    void testLoginUnsuccessful() throws UserAlreadyExistsException {
        // arrange
        // Registrierung eines Bürgers mit gültigen Daten
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
        citizenService.registerCitizen(citizenInput);

        // act
        // Login-Versuch mit falschem Passwort
        boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "blub");

        // assert
        // Der Login sollte fehlschlagen
        assertFalse(loginSuccessful);
    }
}

