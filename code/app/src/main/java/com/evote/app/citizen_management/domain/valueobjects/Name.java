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
     * @param firstName Der Vorname des Bürgers
     * @param lastName Der Nachname des Bürgers
     * @throws IllegalArgumentException wenn Vor- oder Nachname ungültig ist.
     */
    public Name {
        if (!isValidFirstName(firstName)) {
            throw new IllegalArgumentException("Der folgende Vorname ist ungültig: " + firstName);
        }
        if (!isValidLastName(lastName)) {
            throw new IllegalArgumentException("Der folgende Nachname ist ungültig: " + lastName);
        }
    }

    /**
     * Prüft, ob der Vorname nicht null oder leer ist und den Validierungsregeln
     * (nur Buchstaben, 3–10 Zeichen, inkl. Umlaute) entspricht.
     * @param firstName der zu validierende Vorname
     * @return true, wenn der Vorname gültig ist, andernfalls false
     */
    private static boolean isValidFirstName (String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            return false;
        }
        firstName = firstName.trim();
        // Nur Buchstaben. Minimal 3 Zeichen. Maximal 10 Zeichen.
        return firstName.matches("^[A-Za-zÄÖÜäöüß]{3,10}$");
    }

    /**
     *  Prüft, ob der Nachname nicht null ist und den Validierungsregeln
     *  (nur Buchstaben, 3–10 Zeichen, inkl. Umlaute) entspricht.
     * @param lastName der zu validierende Nachname
     * @return true, wenn der Nachname gültig ist, andernfalls false
     */
    private static boolean isValidLastName (String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            return false;
        }
        lastName = lastName.trim();
        // Nur Buchstaben. Minimal 3 Zeichen. Maximal 10 Zeichen.
        return lastName.matches("^[A-Za-zÄÖÜäöüß]{3,10}$");
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
