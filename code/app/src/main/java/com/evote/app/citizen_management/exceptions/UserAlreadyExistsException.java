package com.evote.app.citizen_management.exceptions;

/**
 * Diese Exception wird ausgelöst, wenn bei der Registrierung eine E-Mail-Adresse verwendet wird, die
 * bereits einem bestehenden Nutzer zugeordnet ist.
 *
 * @author Lydia Boes
 * @version 1.0
 */

public class UserAlreadyExistsException extends Exception {

    /**
     * Erstellt eine neue UserAlreadyExistsException mit einer
     * vordefinierten Fehlermeldung.
     */
    public UserAlreadyExistsException(String mail) {
        super("User with email " + mail + " already exists.");
    }
}
