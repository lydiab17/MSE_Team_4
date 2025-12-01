package com.evote.app.citizen_management.application.dto;

/**
 * Request DTO – Client → Server
 * Enthält die Daten für die Registrierung eines Citizens.
 * Was der Client dir schicken darf/muss.
 */
public record CitizenRegistrationRequestDto(
        String firstName,
        String lastName,
        String email,
        String password
) {}


