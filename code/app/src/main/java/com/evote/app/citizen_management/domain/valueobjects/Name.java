package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das einen Namen mit Vor- und Nachname kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Name(String firstName, String lastName) {

    /**
     * Erstellt einen neuen Namen mit Vorname und Nachname.
     *
     * @param firstName Der Vorname des Bürgers. Darf nicht leer oder null sein.
     * @param lastName Der Nachname des Bürgers. Darf nicht leer oder null sein.
     * @throws IllegalArgumentException Wenn Vor- oder Nachname null oder leer ist.
     */
    public Name {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Vorname darf nicht leer sein.");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Der Nachname darf nicht leer sein.");
        }
        firstName = firstName.trim();
        lastName = lastName.trim();
    }

    /**
     * Gibt den vollständigen Namen des Bürgers zurück.
     * @return firstName lastNAme
     */
    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
