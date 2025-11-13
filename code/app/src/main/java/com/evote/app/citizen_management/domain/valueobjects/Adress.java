package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das eine Adresse kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Adress(String street, String zipCode, String city) {

    /**
     * Erstellt einen neuen Namen mit Vorname und Nachname.
     *
     * @param street Die Straße des Bürgers. Darf nicht leer oder null sein.
     * @param zipCode Die PLZ des Bürgers. Darf nicht leer oder null sein.
     * @param city Der Stadt des Bürgers. Darf nicht leer oder null sein.
     * @throws IllegalArgumentException Wenn Straße, PLZ oder Stadt null oder leer ist.
     */
    public Adress {

        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Die Straße darf nicht leer sein");
        }
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Die PLZ darf nicht leer sein");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Die Stadt darf nicht leer sein");
        }

        street = street.trim();
        zipCode = zipCode.trim();
        city = city.trim();
    }

    /**
     * Gibt die vollständige Adresse des Bürgers zurück.
     * @return street zipCode city
     */
    @Override
    public String toString() {
        String sep = System.lineSeparator();
        return street + sep + zipCode + city;
    }
}
