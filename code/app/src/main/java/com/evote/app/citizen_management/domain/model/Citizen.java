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


    private Citizen(Name name, Email email, Password password) {
        this.citizenID = CitizenID.generate();
        this.name = Objects.requireNonNull(name, "Name darf nicht null sein");
        this.email = Objects.requireNonNull(email, "E-Mail darf nicht null sein");
        this.password = Objects.requireNonNull(password, "Passwort darf nicht null sein");
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

}
