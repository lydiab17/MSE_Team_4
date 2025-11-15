package com.evote.app.citizen_management.domain.valueobjects;

/**
 * Record, das eine Email-Adresse kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record Email(String email) {
    /**
     * Erstellt eine neue E-Mail.
     * @param email Die E-Mail-Adresse des Bürgers.
     * @throws IllegalArgumentException wenn die E-Mail-Adresse ungültig ist.
     */
    public Email {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Die E-Mail-Adresse ist ungültig.");
        }
    }

    /**
     * Prüft ob eine E-Mail-Adresse nicht null ist und den Validierungsregeln entspricht.
     * @param email die zu prüfende E-Mail-Adresse
     * @return true, wenn die E-Mail gültig ist, andernfalls false
     */
    private static boolean isValidEmail (String email) {
        if (email == null) {
            return false;
        }
        return email.matches("^(.+)@(\\S+)$");
        // prüft nur, ob das @-Zeichen vorhanden ist
    }
}
