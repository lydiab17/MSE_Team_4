package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das eine Adresse kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Adress(String street, String houseNumber, String zipCode, String city) {

    /**
     * Erstellt eine neue Adresse mit Straße, Hausnummer, PLZ und Stadt.
     *
     * @param street Die Straße des Bürgers.
     * @param houseNumber Die Hausnummer des Bürgers.
     * @param zipCode Die PLZ des Bürgers.
     * @param city Der Stadt des Bürgers.
     * @throws IllegalArgumentException wenn Straße, Hausnummer, PLZ oder Stadt ungültig ist.
     */
    public Adress {

        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Die Straße darf nicht leer sein");
        }
        if (houseNumber == null || houseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Die Hausnummer darf nicht leer sein");
        }
        if (!isValidZipCode(zipCode)) {
            throw new IllegalArgumentException("Ungültige PLZ: " + zipCode);
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("Die Stadt darf nicht leer sein");
        }

        street = street.trim();
        houseNumber = houseNumber.trim();
        city = city.trim();
    }

    /**
     * Prüft, ob eine übergebene Postleitzahl gültig ist.
     * @param zipCode die zu prüfende Postleitzahl
     * @return true, wenn die Postleitzahl gültig ist, andernfalls false
     */
    private static boolean isValidZipCode (String zipCode) {
        if (zipCode == null) {
            return false;
        }
        // PLZ muss exakt 5 Ziffern enthalten
        return zipCode.matches("^\\d{5}$");
    }

    /**
     * Gibt die vollständige Adresse des Bürgers zurück.
     * @return street zipCode city
     */
    @Override
    public String toString() {
        String sep = System.lineSeparator();
        return street + houseNumber + sep + zipCode + city;
    }
}
