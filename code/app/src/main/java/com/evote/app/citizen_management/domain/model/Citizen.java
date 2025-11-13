package com.evote.app.citizen_management.domain.model;

import com.evote.app.citizen_management.domain.valueobjects.Adress;
import com.evote.app.citizen_management.domain.valueobjects.CitizenID;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;

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

    /** Der vollständige Name des Bürgers. */
    private Name name;

    /** Die Adresse des Bürgers. */
    private Adress adress;

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
     * Gibt die Adresse des Bürgers zurück.
     * @return adress
     */
    public Adress getAdress() {
        return adress;
    }
}
