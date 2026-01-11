package citizen_management.application.services;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

// Integrationstest für den CitizenService mit Spring-Kontext
@SpringJUnitConfig(
    classes = {
      CitizenService.class,
      CitizenAggregator.class,
      CitizenProjector.class,
      EventStore.class,
      InMemoryCitizenRepository.class
    })
class CitizenServiceTest {

  // Zu testender Service, wird von Spring injiziert
  @Autowired private CitizenService citizenService;

  // Repository zur direkten Überprüfung und zum Aufräumen nach Tests
  @Autowired private CitizenRepository citizenRepository;

  // Wird nach jedem Test ausgeführt, um Seiteneffekte zwischen Tests zu vermeiden
  @AfterEach
  void cleanUp() {
    citizenRepository.clear();
  }

  // Happy-Path-Test:
  // Prüft, ob ein Citizen mit gültigen Eingabedaten erfolgreich registriert wird
  @Test
  void testRegistrationSuccessful() throws UserAlreadyExistsException {

    // arrange
    // Vorbereitung der gültigen Registrierungsdaten
    CitizenRegistrationRequestDto citizenInput =
        new CitizenRegistrationRequestDto(
            "Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

    // act
    // Ausführung der zu testenden Methode
    Citizen citizen = citizenService.registerCitizen(citizenInput);

    // assert
    // Überprüfung, ob die gespeicherten Daten korrekt übernommen wurden
    assertEquals("Max", citizen.getName().firstName());
    assertEquals("Mustermann", citizen.getName().lastName());
    assertEquals("max.mustermann@test.de", citizen.getEmail().email());
  }

  // Negativtest:
  // Prüft, ob eine Registrierung mit zu kurzem Passwort abgelehnt wird
  @Test
  void testRegistrationUnsuccessfulShortPW() throws IllegalArgumentException {

    // arrange
    // Vorbereitung von ungültigen Registrierungsdaten (Passwort zu kurz)
    CitizenRegistrationRequestDto citizenInput =
        new CitizenRegistrationRequestDto("Max", "Mustermann", "max.mustermann@test.de", "ohnepw");

    // act and check
    // Erwartet, dass beim Registrierungsversuch eine IllegalArgumentException geworfen wird
    assertThrows(
        IllegalArgumentException.class, () -> citizenService.registerCitizen(citizenInput));
  }

  // Happy-Path-Test:
  // Prüft, ob sich ein registrierter Citizen mit korrekten Zugangsdaten erfolgreich einloggen kann
  @Test
  void testLoginSuccessful() throws UserAlreadyExistsException {

    // arrange
    // Registrierung eines gültigen Citizens als Voraussetzung für den Login
    CitizenRegistrationRequestDto citizenInput =
        new CitizenRegistrationRequestDto(
            "Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
    Citizen citizen = citizenService.registerCitizen(citizenInput);

    // act
    // Login mit korrekter E-Mail-Adresse und richtigem Passwort
    boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "testtest1234");

    // assert
    // Erwartung: Login ist erfolgreich
    assertTrue(loginSuccessful);
  }

  // Negativtest:
  // Prüft, ob der Login mit falschem Passwort fehlschlägt
  @Test
  void testLoginUnsuccessful() throws UserAlreadyExistsException {

    // arrange
    // Registrierung eines Citizens als Voraussetzung für den Login-Versuch
    CitizenRegistrationRequestDto citizenInput =
        new CitizenRegistrationRequestDto(
            "Max", "Mustermann", "max.mustermann@test.de", "testtest1234");
    citizenService.registerCitizen(citizenInput);

    // act
    // Login-Versuch mit korrekter E-Mail-Adresse, aber falschem Passwort
    boolean loginSuccessful = citizenService.loginCitizen("max.mustermann@test.de", "blub");

    // assert
    // Erwartung: Login ist nicht erfolgreich
    assertFalse(loginSuccessful);
  }
}
