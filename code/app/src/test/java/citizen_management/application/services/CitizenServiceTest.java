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
public class CitizenServiceTest {

    @Autowired
    private CitizenService citizenService;

    @Autowired
    private CitizenRepository citizenRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void cleanUp() {
        citizenRepository.clear();
    }

    @Test
    void testRegistrationSuccessful() throws UserAlreadyExistsException {
        // arrange
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

        // act
        Citizen citizen = citizenService.registerCitizen(citizenInput);

        // assert
        assertEquals("Max", citizen.getName().firstName());
        assertEquals("Mustermann", citizen.getName().lastName());
        assertEquals("max.mustermann@test.de", citizen.getEmail().email());
    }

    @Test
    void testRegistrationUnsuccessfulShortPW() throws IllegalArgumentException {
        // arrange
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "ohnepw");

        // act and check
        assertThrows(IllegalArgumentException.class, () -> citizenService.registerCitizen(citizenInput));
    }

    @Test
    void testLoginSuccessful() throws UserAlreadyExistsException {
        // arrange
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
        Citizen citizen = citizenService.registerCitizen(citizenInput);

        // act
        boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "testtest1234");

        // assert
        assertTrue(loginSuccessful);
    }

    @Test
    void testLoginUnsuccessful() throws UserAlreadyExistsException {
        // arrange
        CitizenRegistrationRequestDto citizenInput = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
        citizenService.registerCitizen(citizenInput);

        // act
        boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "blub");

        // assert
        assertFalse(loginSuccessful);
    }
}

