package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.*;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.application.services.TokenService;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST-Controller für den Zugriff auf Citizen-Use-Cases über HTTP.
 *
 * <p>Der {@code CitizenRestController} dient als technischer Adapter zwischen der HTTP-Schicht und
 * der Application-Schicht. Er nimmt HTTP-Requests entgegen, wandelt eingehende JSON-Daten in DTOs
 * um und delegiert die fachliche Verarbeitung an den {@link CitizenService}.
 *
 * <p>Der Controller enthält keine Geschäftslogik. Er ist ausschließlich für die Kommunikation über
 * HTTP sowie für die Übersetzung von Service-Ergebnissen in HTTP-Responses zuständig.
 */
@RestController
@RequestMapping("/api/citizens")
public class CitizenRestController {

  /** Application Service zur Ausführung der Citizen-Use-Cases. */
  private final CitizenService citizenService;

  /**
   * Erstellt einen neuen {@code CitizenRestController}.
   *
   * @param citizenService Service zur Ausführung der fachlichen Citizen-Use-Cases
   */
  public CitizenRestController(CitizenService citizenService) {
    this.citizenService = citizenService;
  }

  /**
   * Registriert einen neuen Bürger im System.
   *
   * <p>Die übergebenen Registrierungsdaten werden an den {@link CitizenService} weitergeleitet. Das
   * vom Service zurückgegebene Domain-Objekt wird anschließend in ein Response-DTO umgewandelt.
   *
   * @param request DTO mit den Registrierungsdaten des Bürgers
   * @return Response-DTO mit den Daten des neu registrierten Bürgers
   * @throws UserAlreadyExistsException wenn bereits ein Bürger mit der angegebenen E-Mail existiert
   */
  @PostMapping("/register")
  public CitizenRegistrationResponseDto register(@RequestBody CitizenRegistrationRequestDto request)
      throws UserAlreadyExistsException {
    Citizen c = citizenService.registerCitizen(request);

    return CitizenRegistrationResponseDto.fromDomain(c);
  }

  /**
   * Authentifiziert einen Bürger anhand von E-Mail und Passwort.
   *
   * <p>Bei erfolgreichem Login wird ein Authentifizierungs-Token erzeugt und sowohl im
   * Response-Body als auch im {@code Authorization}-Header zurückgegeben. Bei fehlgeschlagener
   * Authentifizierung wird der HTTP-Status 401 zurückgegeben.
   *
   * @param request DTO mit Login-Daten (E-Mail und Passwort)
   * @param response HTTP-Response zum Setzen des Authorization-Headers
   * @return {@link ResponseEntity} mit Token bei Erfolg oder HTTP 401 bei Fehlschlag
   */
  @PostMapping("/login")
  public ResponseEntity<String> login(
      @RequestBody CitizenLoginRequestDto request, HttpServletResponse response) {
    boolean b = citizenService.loginCitizen(request.email(), request.password());

    if (!b) {
      return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
    }

    String token = TokenService.generateToken(request.email());
    response.addHeader("Authorization", "Bearer " + token);
    return ResponseEntity.ok(token);
  }

  /**
   * Liefert die Daten des aktuell eingeloggten Bürgers.
   *
   * <p>Der aktuell authentifizierte Benutzer wird über den Security-Kontext ermittelt. Die
   * zugehörigen Bürgerdaten werden über den {@link CitizenService} geladen und als Response-DTO
   * zurückgegeben.
   *
   * @param request HTTP-Request (z. B. für Authentifizierungsinformationen)
   * @return Response-DTO mit den Daten des eingeloggten Bürgers
   */
  @GetMapping("/citizen")
  public CitizenResponseDto getLoggedInCitizen(HttpServletRequest request) {
    return CitizenResponseDto.fromDomain(citizenService.getCurrentLoggedInCitizen());
  }
}
