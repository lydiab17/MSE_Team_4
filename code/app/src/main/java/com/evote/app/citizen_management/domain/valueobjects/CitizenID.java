package com.evote.app.citizen_management.domain.valueobjects;

import java.util.Objects;
import java.util.UUID;

/**
 * Record, das eine ID vom Bürger kapselt.
 *
 * @author Lydia Boes
 * @version 1.0
 */
public record CitizenID(UUID id) {

    /**
     * Konstruktor, der sicherstellt, dass die übergebene UUID nicht null ist.
     *
     * @param id eindeutige UUID des Bürgers
     * @throws NullPointerException wenn id null ist
     */
    public CitizenID {
        Objects.requireNonNull(id, "Die CitizenID darf nicht null sein");
    }

    /**
     * Erzeugt eine neue, zufällige und gültige CitizenID
     *
     * @return neue CitizenID mit zufällig generierter UUID
     */
    public static CitizenID generate() {
        return new CitizenID(UUID.randomUUID());
        // UUID = Universally Unique Identifier: 128-Bit große zufällige Zahl
    }

    /**
     * Erzeugt eine CitizenID aus einem String, der eine gültige UUID im Standardformat repräsentieren muss.
     *
     * @param uuidString eine UUID als String
     * @return eine CitizenID, die auf der gegebenen UUID basiert
     * @throws IllegalArgumentException wenn der String kein gültiges UUID-Format hat
     */
    public static CitizenID fromString(String uuidString) {
        try {
            return new CitizenID(UUID.fromString(uuidString));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Ungültiges UUID-Format: " + uuidString);
        }
    }

    /**
     * Gibt die UUID als String zurück.
     *
     * @return Textdarstellung der UUID
     */
    @Override
    public String toString() {
        return id.toString();
    }
}
