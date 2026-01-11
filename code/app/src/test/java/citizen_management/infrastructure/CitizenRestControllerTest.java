package citizen_management.infrastructure;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

// Unit-Test für den CitizenRestController.
@ExtendWith(MockitoExtension.class)
class CitizenRestControllerTest {

  @InjectMocks private CitizenRestController controller;

  // CitizenService ist ein Mock-Objekt
  @Mock private CitizenService citizenService;

  // Happy-Path-Test:
  // Erfolgreiche Registrierung eines neuen Bürgers.
  @Test
  void testUserCreatedSuccessful() throws UserAlreadyExistsException {
    // Arrange
    CitizenRegistrationRequestDto citizenRegistrationRequestDto =
        new CitizenRegistrationRequestDto("Max", "Mustermann", "test@test.de", "testtest1234");

    // Service simuliert erfolgreiche Registrierung
    when(citizenService.registerCitizen(any()))
        .thenReturn(
            Citizen.create(
                new Name(
                    citizenRegistrationRequestDto.firstName(),
                    citizenRegistrationRequestDto.lastName()),
                new Email(citizenRegistrationRequestDto.email()),
                new Password(citizenRegistrationRequestDto.password())));

    // Act
    CitizenRegistrationResponseDto citizenRegistrationResponseDto =
        controller.register(citizenRegistrationRequestDto);

    // Assert
    assertEquals("Max", citizenRegistrationResponseDto.firstName());
    assertEquals("Mustermann", citizenRegistrationResponseDto.lastName());
    assertEquals("test@test.de", citizenRegistrationResponseDto.email());
  }

  // Negativ-Test:
  // Registrierung schlägt fehl, da Benutzer bereits existiert.
  @Test
  void testUserCreatedErrorUserAlreadyExists() throws UserAlreadyExistsException {
    // Arrange
    CitizenRegistrationRequestDto citizenRegistrationRequestDto =
        new CitizenRegistrationRequestDto(
            "Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

    // Service simuliert erfolgreiche erste Registrierung
    when(citizenService.registerCitizen(any()))
        .thenReturn(
            Citizen.create(
                new Name(
                    citizenRegistrationRequestDto.firstName(),
                    citizenRegistrationRequestDto.lastName()),
                new Email(citizenRegistrationRequestDto.email()),
                new Password(citizenRegistrationRequestDto.password())));

    controller.register(citizenRegistrationRequestDto);

    CitizenRegistrationRequestDto anotherCitizenRegistrationRequestDto =
        new CitizenRegistrationRequestDto(
            "Erika", "Mustermann", "max.mustermann@test.de", "testtest1234");

    // Service simuliert Duplicate-User-Fehler
    when(citizenService.registerCitizen(any()))
        .thenThrow(new UserAlreadyExistsException(citizenRegistrationRequestDto.email()));

    // Act & Assert
    assertThrows(
        UserAlreadyExistsException.class,
        () -> controller.register(anotherCitizenRegistrationRequestDto));
  }

  // Happy-Path-Test:
  // Erfolgreicher Login.
  @Test
  void testUserLoginSuccessful() throws UserAlreadyExistsException {
    // Arrange
    CitizenLoginRequestDto login =
        new CitizenLoginRequestDto("max.mustermann@test.de", "testtest1234");

    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    // Service bestätigt erfolgreichen Login
    when(citizenService.loginCitizen(login.email(), login.password())).thenReturn(true);

    // Act
    ResponseEntity<String> responseEntity = controller.login(login, servletResponse);

    // Assert
    assertNotNull(responseEntity);
    assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    assertTrue(responseEntity.hasBody());
  }

  // Negativ-Test:
  // Login schlägt fehl.
  @Test
  void testUserLoginInvalidLoginData() {
    // Arrange
    CitizenLoginRequestDto login =
        new CitizenLoginRequestDto("max.mustermann@test.de", "testtest1234");

    HttpServletResponse servletResponse = mock(HttpServletResponse.class);

    // Service simuliert fehlgeschlagenen Login
    when(citizenService.loginCitizen(login.email(), login.password())).thenReturn(false);

    // Act
    ResponseEntity<String> responseEntity = controller.login(login, servletResponse);

    // Assert
    assertNotNull(responseEntity);
    assertTrue(responseEntity.getStatusCode().is4xxClientError());
    assertFalse(responseEntity.hasBody());
  }
}
