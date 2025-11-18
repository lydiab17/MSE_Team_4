package com.evote.app.citizen_management.domain.model;

import com.evote.app.citizen_management.domain.valueobjects.*;

import java.util.Objects;

/**
 * Diese Klasse repräsentiert einen Bürger.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public class Citizen {

    /** Eindeutige Identifikationsnummer des Bürgers. */
    private CitizenID citizenID;

    /** Die Email-Adresse des Bürgers. */
    private Email email;

    /** Das Passwort des Bürgers. */
    private Password password;

    /** Der vollständige Name des Bürgers. */
    private Name name;


    /**
     * Privater Konstruktor, der ein neues Citizen-Objekt erstellt.
     * @param name der vollständige Name des Bürgers
     * @param email die E-Mail-Adresse des Bürgers
     * @param password das Passwort des Bürgers
     * @throws NullPointerException falls einer der Parameter null ist
     */
    private Citizen(Name name, Email email, Password password) {
        this.citizenID = CitizenID.generate();
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein");
        this.email = Objects.requireNonNull(email, "E-Mail darf nicht null sein");
        this.password = Objects.requireNonNull(password, "Passwort darf nicht null sein");
    }

    /**
     * Fabrikmethode zum Erzeugen eines neuen Citizen-Objekts.
     * @param name der Name des Bürgers
     * @param email die E-Mail des Bürgers
     * @param password das Passwort des Bürgers
     * @return eine neue, gültige Citizen-Instanz
     */
    public static Citizen create(Name name, Email email, Password password) {
        return new Citizen(name, email, password);
    }


    /**
     * Gibt die ID des Bürgers zurück.
     * @return citizenID
     */
    public CitizenID getCitizenID() {
        return citizenID;
    }

    /**
     * Gibt die E-Mail-Adresse des Bürgers zurück.
     * @return email
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Gibt den Namen des Bürgers zurück.
     * @return name
     */
    public Name getName() {
        return name;
    }

    /**
     * Gibt das Passwort des Bürgers zurück.
     * @return password
     */
    public Password getPassword() {
        return password;
    }

}
