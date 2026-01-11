package citizen_management.infrastructure;

import static org.junit.jupiter.api.Assertions.*;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Integrationstest für den CitizenApiClient.
// Startet den kompletten Spring-Kontext inklusive Webserver
// auf einem definierten Port, um echte HTTP-Aufrufe zu testen.
@SpringBootTest(
    classes = com.evote.app.EvoteSpringConfig.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class CitizenApiClientTest {

  // Der zu testende API-Client wird aus dem Spring-Kontext injiziert.
  // Dadurch wird sichergestellt, dass Konfiguration, REST-Client
  // und Serialisierung korrekt funktionieren.
  @Autowired CitizenApiClient client;

  // Happy-Path-Test:
  // Verifiziert, dass ein Bürger erfolgreich registriert werden kann
  // und anschließend mit denselben Zugangsdaten ein Login möglich ist.
  @Test
  void register_and_login_work() {
    // Testdaten für einen neuen Bürger
    String email = "test@example.com";
    String pw = "Secret123";

    // Registrierung des Bürgers sollte erfolgreich sein
    boolean registered = client.registerCitizen("Max", "Mustermann", email, pw);
    assertTrue(registered);

    // Nach erfolgreicher Registrierung sollte ein Login
    // ein gültiges (nicht leeres) Authentifizierungs-Token liefern
    var token = client.loginAndGetToken(email, pw);
    assertTrue(token.isPresent());
    assertFalse(token.get().isBlank());
  }

  // Negativtest:
  // Stellt sicher, dass ein Login mit falschem Passwort
  // korrekt fehlschlägt und kein Token zurückgegeben wird.
  @Test
  void login_fails_with_wrong_password() {
    // Korrekte Zugangsdaten für die Registrierung
    String email = "test2@example.com";
    String pw = "Secret123";

    // Registrierung muss zunächst erfolgreich sein,
    // damit der Login-Test sinnvoll ist
    assertTrue(client.registerCitizen("Max", "Mustermann", email, pw));

    // Login mit falschem Passwort:
    // Erwartet wird ein leeres Optional (kein Token)
    assertTrue(client.loginAndGetToken(email, "wrong").isEmpty());
  }
}
