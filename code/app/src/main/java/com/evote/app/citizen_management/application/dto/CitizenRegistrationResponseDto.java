package com.evote.app.citizen_management.application.dto;

import com.evote.app.citizen_management.domain.model.Citizen;

/**
 * Response DTO – Server → Client
 * Ergebnis der Registrierung eines Citizens.
 * Was du dem Client zurückgeben willst/darfst.
 */
public record CitizenRegistrationResponseDto(
        String firstName,
        String lastName,
        String email
) {
    public static CitizenRegistrationResponseDto fromDomain(Citizen c) {
        return new CitizenRegistrationResponseDto(
                c.getName().firstName(),
                c.getName().lastName(),
                c.getEmail().email()
        );
    }
}


