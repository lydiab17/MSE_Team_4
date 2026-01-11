package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.CitizenLoginRequestDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Infrastruktur-Komponente zur Kommunikation mit dem Citizen-Backend. Diese Klasse kapselt
 * HTTP-Aufrufe für Registrierung und Login und übernimmt die Serialisierung der Request-Daten.
 */
@Component
public class CitizenApiClient {

  /** Basis-URL des Citizen-REST-Endpunkts. */
  private static final String BASE_URL = "http://localhost:8080/api/citizens";

  /**
   * Registriert einen neuen Bürger über das Backend.
   *
   * @param firstName Vorname des Bürgers
   * @param lastName Nachname des Bürgers
   * @param email E-Mail-Adresse des Bürgers
   * @param password Passwort des Bürgers
   * @return true, wenn die Registrierung erfolgreich war, sonst false
   */
  public boolean registerCitizen(String firstName, String lastName, String email, String password) {
    try {
      var payload = new CitizenRegistrationRequestDto(firstName, lastName, email, password);
      var json = new ObjectMapper().writeValueAsString(payload);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(BASE_URL + "/register"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {

        return true;
      } else {
        return false;
      }

    } catch (Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Führt einen Login-Vorgang über das Backend aus und liefert bei Erfolg ein
   * Authentifizierungs-Token zurück.
   *
   * @param email E-Mail-Adresse des Bürgers
   * @param password Passwort des Bürgers
   * @return Optional mit Token bei erfolgreichem Login, sonst Optional.empty()
   */
  public Optional<String> loginAndGetToken(String email, String password) {
    try {
      var payload = new CitizenLoginRequestDto(email, password);
      var json = new ObjectMapper().writeValueAsString(payload);

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(BASE_URL + "/login"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build();

      var response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        return Optional.ofNullable(response.body()).filter(s -> !s.isBlank());
      }
      return Optional.empty();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }
}
