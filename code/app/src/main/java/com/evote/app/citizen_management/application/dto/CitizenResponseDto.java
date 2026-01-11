package com.evote.app.citizen_management.application.dto;

/**
 * Data Transfer Object (DTO) zur Rückgabe von Citizen-Daten an den Client.
 *
 * <p>Dieses Response-DTO enthält ausschließlich die Informationen,
 * die für die Darstellung eines Bürgers nach außen vorgesehen sind,
 * und kapselt damit die interne Datenstruktur.</p>
 *
 * @param email die E-Mail-Adresse des Bürgers
 * @param vorname der Vorname des Bürgers
 * @param nachname der Nachname des Bürgers
 */
public record CitizenResponseDto(String email, String vorname, String nachname) {

    /**
     * Erstellt ein {@link CitizenResponseDto} aus einem {@link CitizenDto}.
     *
     * <p>Die Methode übernimmt nur die für den Client relevanten Felder
     * und stellt damit eine saubere Trennung zwischen interner
     * Datenrepräsentation und externer Antwort sicher.</p>
     *
     * @param c das CitizenDto, aus dem die Daten übernommen werden
     * @return ein neues {@link CitizenResponseDto}
     */
    public static CitizenResponseDto fromDomain(CitizenDto c) {
        return new CitizenResponseDto(c.getEmail(), c.getVorname(), c.getNachname());
    }
}
