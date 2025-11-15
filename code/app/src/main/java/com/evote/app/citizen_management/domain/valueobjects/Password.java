package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das ein Passwort kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Password(String password) {

    /**
     * Erstellt ein neues Passwort.
     * @param password das zu setzende Passwort im Klartext
     * @throws IllegalArgumentException wenn das Passwort ungültig ist.
     */
    public Password {
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Das Passwort ist ungültig.");
        }
    }

    /**
     * Prüft ob ein Passwort nicht null ist und den Validierungsregeln entspricht.
     * @param password das zu prüfende Passwort im Klartext
     * @return true, wenn das Passwort gültig ist, andernfalls false
     */
    private static boolean isValidPassword (String password) {
        if (password == null) {
            return false;
        }
        return password.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");
        // Mindestens ein Buchstabe, mindestens eine Zahl, mindestens 8 Zeichen lang
    }
}
