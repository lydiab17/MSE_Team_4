package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.CitizenLoginRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * API-Client zur Kommunikation mit dem Citizen-Backend.
 * Diese Klasse kapselt die HTTP-Kommunikation mit dem Backend-Service
 * für die Registrierung und Anmeldung von Bürgern. Sie erstellt die
 * benötigten HTTP-Anfragen, serialisiert (umwandeln)  die Request-Daten in JSON
 * und wertet die HTTP-Antworten aus.
 *
 * @author Lydia Boes, Fabian Schmitz
 * @version 2.0
 */
@Component
public class CitizenApiClient {
    /**
     * Basis-URL des Citizen-Backend-Services.
     */
    private static final String BASE_URL = "http://localhost:8080/api/citizens";


    /**
     * Sendet einen Registrierungs-Request an das Backend und wertet das Ergebnis aus.
     *
     * @param firstName der Vorname des Bürgers
     * @param lastName  der Nachname des Bürgers
     * @param email     die E-Mail-Adresse des Bürgers
     * @param password  das Passwort des Bürgers
     * @return true, wenn die Registrierung erfolgreich war, andernfalls false
     */
    public boolean registerCitizen(String firstName, String lastName, String email, String password) {
        try {
            // Erstellen eines DTO-Objekts mit den Registrierungsdaten aus der UI
            var payload = new CitizenRegistrationRequestDto(firstName, lastName, email, password);

            // Umwandeln des DTO-Objekts in einen JSON-String
            var json = new ObjectMapper().writeValueAsString(payload);

            // Erstellen eines HTTP-Clients für die Kommunikation mit dem Backend
            HttpClient client = HttpClient.newHttpClient();

            // Aufbau der HTTP-POST-Anfrage an den /register-Endpunkt
            HttpRequest request = HttpRequest.newBuilder()
                    // Ziel-URL des Backend-Endpunkts
                    .uri(URI.create(BASE_URL + "/register"))
                    // Setzen des Content-Typs auf JSON
                    .header("Content-Type", "application/json")
                    // Anhängen des JSON-Strings als Request-Body
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    // Fertigstellen der Anfrage
                    .build();

            // Senden der Anfrage an das Backend und Warten auf die Antwort
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Statuscode 200 bedeutet: Registrierung erfolgreich
            if (response.statusCode() == 200) {
                return true;
            } else {
                // Jeder andere Statuscode bedeutet: Registrierung fehlgeschlagen
                return false;
            }

        } catch (Exception ex) {
            // Fehler bei JSON-Erzeugung, Netzwerkkommunikation oder Antwortverarbeitung
            ex.printStackTrace();
            // Bei jedem Fehler wird false zurückgegeben
            return false;
        }
    }


    /**
     * Sendet Login-Daten an das Backend und liefert bei erfolgreicher Anmeldung
     * das vom Backend erzeugte Authentifizierungs-Token zurück.
     *
     * @param email    die E-Mail-Adresse des Benutzers
     * @param password das Passwort des Benutzers
     * @return ein Optional mit dem Authentifizierungs-Token bei erfolgreichem Login, andernfalls Optional#empty()
     */
    public Optional<String> loginAndGetToken(String email, String password) {
        try {
            // Erstellen eines DTO-Objekts mit den Login-Daten aus der UI
            var payload = new CitizenLoginRequestDto(email, password);

            // Umwandeln des DTO-Objekts in einen JSON-String
            var json = new ObjectMapper().writeValueAsString(payload);

            // Erstellen eines HTTP-Clients für die Kommunikation mit dem Backend
            HttpClient client = HttpClient.newHttpClient();

            // Aufbau der HTTP-POST-Anfrage an den /login-Endpunkt
            HttpRequest request = HttpRequest.newBuilder()
                    // Ziel-URL des Login-Endpunkts
                    .uri(URI.create(BASE_URL + "/login"))
                    // Setzen des Content-Typs auf JSON
                    .header("Content-Type", "application/json")
                    // Anhängen der Login-Daten als JSON im Request-Body
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    // Fertigstellen der Anfrage
                    .build();

            // Senden der Anfrage an das Backend und Warten auf die Antwort
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Statuscode 200 bedeutet: Login erfolgreich
            if (response.statusCode() == 200) {
                // Liest den Response-Body, der bei erfolgreichem Login das Authentifizierungs-Token enthält,
                // und verpackt ihn in ein Optional (null-sichere Rückgabe)
                return Optional.ofNullable(response.body())
                        .filter(s -> !s.isBlank());
                // Filtert leere oder nur aus Leerzeichen bestehende Antworten heraus,
                // sodass nur ein gültiges Token zurückgegeben wird
            }

            // Jeder andere Statuscode bedeutet: Login fehlgeschlagen - kein Token vorhanden
            // leeres Optional zurückgeben
            return Optional.empty();

        } catch (Exception ex) {
            // Fehler bei JSON-Erzeugung, Netzwerkkommunikation oder Antwortverarbeitung
            ex.printStackTrace();
            // Bei einem Fehler wird kein Token zurückgegeben
            return Optional.empty();
        }
    }

}