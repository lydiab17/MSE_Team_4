package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das einen Namen mit Vor- und Nachname kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Name(String firstName, String lastName) {


    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 10;

    // Regex-Basis: erlaubt nur Buchstaben inkl. Umlaute
    private static final String NAME_CHAR_PATTERN = "A-Za-zÄÖÜäöüß";
    private static final String NAME_PATTERN =
            "^[" + NAME_CHAR_PATTERN + "]{" + MIN_NAME_LENGTH + "," + MAX_NAME_LENGTH + "}$";

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

        // Nur Buchstaben. Minimal 3 Zeichen. Maximal 10 Zeichen.
        return firstName.matches(NAME_PATTERN);
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

        // Nur Buchstaben. Minimal 3 Zeichen. Maximal 10 Zeichen.
        return lastName.matches(NAME_PATTERN);
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
