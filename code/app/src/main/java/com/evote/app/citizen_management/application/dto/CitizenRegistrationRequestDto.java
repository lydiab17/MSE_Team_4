package com.evote.app.citizen_management.application.dto;

/**
 * Data Transfer Object (DTO) für Registrierungsanfragen eines Bürgers.
 *
 * <p>Dieses Request-DTO wird vom Client an den Server gesendet und enthält alle notwendigen Daten,
 * die für die Registrierung eines neuen Bürgers erforderlich sind.
 *
 * @param firstName der Vorname des Bürgers
 * @param lastName der Nachname des Bürgers
 * @param email die E-Mail-Adresse des Bürgers
 * @param password das Passwort des Bürgers
 */
public record CitizenRegistrationRequestDto(
    String firstName, String lastName, String email, String password) {}
