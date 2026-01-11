package com.evote.app.citizen_management.application.dto;

/**
 * Data Transfer Object (DTO) für Login-Anfragen eines Bürgers.
 *
 * <p>Dieses Record dient zur Übertragung der für den Login
 * erforderlichen Zugangsdaten. </p>
 *
 * @param email die E-Mail-Adresse des Bürgers
 * @param password das Passwort des Bürgers
 */
public record CitizenLoginRequestDto(String email, String password) {}