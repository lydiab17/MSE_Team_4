package com.evote.app.citizen_management.exceptions;

/**
 * Diese Exception wird ausgelöst, wenn bei der Registrierung eines Citizens eine E-Mail-Adresse
 * angegeben wird, die bereits einem bestehenden Benutzerkonto zugeordnet ist.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public class UserAlreadyExistsException extends Exception {

  /**
   * Konstruktor der UserAlreadyExistsException.
   *
   * <p>Erstellt eine neue Exception mit einer Fehlermeldung, die die betroffene E-Mail-Adresse
   * enthält.
   *
   * @param mail die E-Mail-Adresse, die bereits registriert ist
   */
  public UserAlreadyExistsException(String mail) {
    super("User with email " + mail + " already exists.");
  }
}
