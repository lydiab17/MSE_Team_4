package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.application.dto.CitizenLoginRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CitizenApiClient {
    private static final String BASE_URL = "http://localhost:8080/api/citizens";

    public boolean loginCitizen(String email, String password) {
        try {
            var payload = new CitizenLoginRequestDto( email, password);
            var json = new ObjectMapper().writeValueAsString(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Hier: " + response.statusCode());
            if (response.statusCode() == 200) {
                return true;
            }

            return false;

        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean registerCitizen(String firstName, String lastName, String email, String password) {
        try {
            var payload = new CitizenRegistrationRequestDto(firstName, lastName, email, password);
            var json = new ObjectMapper().writeValueAsString(payload);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Hier: " + response.statusCode());
            if (response.statusCode() == 403) {
                // Vom Backend geworfene UserAlreadyExistsException
                System.out.println("geht nicsch");
            }
            if(response.statusCode() == 200) {
                System.out.println(response.statusCode());
                return true;
            } else {
                return false;
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
