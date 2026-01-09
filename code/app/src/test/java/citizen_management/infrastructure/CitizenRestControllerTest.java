package citizen_management.infrastructure;

import com.evote.app.citizen_management.application.CitizenAggregator;
import com.evote.app.citizen_management.application.dto.CitizenLoginRequestDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationResponseDto;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenRestController;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenRestControllerTest {

    @InjectMocks
    private CitizenRestController controller;

    @Mock
    private CitizenService citizenService;

    @Mock
    private CitizenAggregator citizenAggregator;

    @Test
    void testUserCreatedSuccessful() throws UserAlreadyExistsException {
        // input
        CitizenRegistrationRequestDto citizenRegistrationRequestDto = new CitizenRegistrationRequestDto("Max", "Mustermann", "test@test.de", "testtest1234");

        // mock service
        when(citizenService.registerCitizen(any())).thenReturn(Citizen.create(new Name(citizenRegistrationRequestDto.firstName(), citizenRegistrationRequestDto.lastName()),
                new Email(citizenRegistrationRequestDto.email()),
                new Password(citizenRegistrationRequestDto.password())));

        // verify
        CitizenRegistrationResponseDto citizenRegistrationResponseDto = controller.register(citizenRegistrationRequestDto);

        // check
        assertEquals("Max", citizenRegistrationResponseDto.firstName());
        assertEquals("Mustermann", citizenRegistrationResponseDto.lastName());
        assertEquals("test@test.de", citizenRegistrationResponseDto.email());
    }

    @Test
    void testUserCreatedErrorUserAlreadyExists() throws UserAlreadyExistsException {
        // create first user (Preconditions)
        CitizenRegistrationRequestDto citizenRegistrationRequestDto = new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

        when(citizenService.registerCitizen(any())).thenReturn(Citizen.create(new Name(citizenRegistrationRequestDto.firstName(), citizenRegistrationRequestDto.lastName()),
                new Email(citizenRegistrationRequestDto.email()),
                new Password(citizenRegistrationRequestDto.password())));

        controller.register(citizenRegistrationRequestDto);

        // create second user with same email (maybe from her husband)
        CitizenRegistrationRequestDto anotherCitizenRegistrationRequestDto = new CitizenRegistrationRequestDto("Erika", "Mustermann", "max.mustermann@test.de", "testtest1234");

        when(citizenService.registerCitizen(any())).thenThrow(new UserAlreadyExistsException(citizenRegistrationRequestDto.email()));
        assertThrows(UserAlreadyExistsException.class, () -> controller.register(anotherCitizenRegistrationRequestDto));
    }

    @Test
    void testUserLoginSuccessful() throws UserAlreadyExistsException {
        // input
        CitizenLoginRequestDto login = new CitizenLoginRequestDto("max.mustermann@test.de", "testtest1234");

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // check
        when(citizenService.loginCitizen(login.email(), login.password())).thenReturn(true);
        ResponseEntity<String> responseEntity = controller.login(login, servletResponse);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.hasBody());
    }


    @Test
    void testUserLoginInvalidLoginData() {
        // input
        CitizenLoginRequestDto login = new CitizenLoginRequestDto("max.mustermann@test.de", "testtest1234");

        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // check
        when(citizenService.loginCitizen(login.email(), login.password())).thenReturn(false);
        ResponseEntity<String> responseEntity = controller.login(login, servletResponse);

        assertNotNull(responseEntity);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertFalse(responseEntity.hasBody());
    }
}
