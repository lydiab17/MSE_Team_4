package com.evote.app.citizen_management.application.commands;

/**
 * Command-Objekt für den Use Case „Registrierung eines Bürgers“.
 * Befehl, der eine Zustandsänderung auslösen soll.
 *
 * @param vorname Vorname des zu registrierenden Bürgers
 * @param nachname Nachname des zu registrierenden Bürgers
 * @param email E-Mail-Adresse des Bürgers (eindeutiger Identifikator)
 * @param password Passwort des Bürgers
 */
public record CitizenRegistrationCommand(
        String vorname, String nachname, String email, String password) {}
