package com.evote.app.citizen_management.application.dto;

import com.evote.app.citizen_management.domain.model.Citizen;

/**
 * Data Transfer Object (DTO) für Registrierungsantworten eines Bürgers.
 *
 * <p>Dieses Response-DTO wird vom Server an den Client zurückgegeben
 * und enthält ausschließlich die Informationen, die nach einer
 * erfolgreichen Registrierung offengelegt werden dürfen.</p>
 *
 *
 * @param firstName der Vorname des Bürgers
 * @param lastName der Nachname des Bürgers
 * @param email die E-Mail-Adresse des Bürgers
 */
public record CitizenRegistrationResponseDto(String firstName, String lastName, String email) {

    /**
     * Erstellt ein {@link CitizenRegistrationResponseDto} aus einem
     * {@link Citizen}-Domain-Objekt.
     *
     * <p>Es werden nur die für den Client relevanten und erlaubten
     * Informationen übernommen.</p>
     *
     * @param c das Citizen-Domain-Objekt
     * @return ein neues {@link CitizenRegistrationResponseDto}
     */
    public static CitizenRegistrationResponseDto fromDomain(Citizen c) {
        return new CitizenRegistrationResponseDto(
                c.getName().firstName(), c.getName().lastName(), c.getEmail().email());
    }
}
