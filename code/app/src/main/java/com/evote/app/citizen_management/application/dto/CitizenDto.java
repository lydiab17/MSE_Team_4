package com.evote.app.citizen_management.application.dto;

/**
 * Data Transfer Object (DTO) zur Übertragung von Citizen-Daten
 * zwischen Anwendungsschichten oder über Schnittstellen.
 *
 * <p>Diese Klasse enthält ausschließlich einfache Datenfelder
 * ohne Geschäftslogik und dient dem strukturierten Austausch
 * von Bürgerinformationen.</p>
 */
public class CitizenDto {

    /**
     * Eindeutige Benutzer-ID des Bürgers.
     */
    private String userId;

    /**
     * Vorname des Bürgers.
     */
    private String vorname;

    /**
     * Nachname des Bürgers.
     */
    private String nachname;

    /**
     * E-Mail-Adresse des Bürgers.
     */
    private String email;

    /**
     * Passwort des Bürgers.
     */
    private String password;

    /**
     * Liefert die Benutzer-ID des Bürgers.
     *
     * @return die Benutzer-ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Setzt die Benutzer-ID des Bürgers.
     *
     * @param userId die Benutzer-ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Liefert den Vornamen des Bürgers.
     *
     * @return der Vorname
     */
    public String getVorname() {
        return vorname;
    }

    /**
     * Setzt den Vornamen des Bürgers.
     *
     * @param vorname der Vorname
     */
    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    /**
     * Liefert den Nachnamen des Bürgers.
     *
     * @return der Nachname
     */
    public String getNachname() {
        return nachname;
    }

    /**
     * Setzt den Nachnamen des Bürgers.
     *
     * @param nachname der Nachname
     */
    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    /**
     * Liefert die E-Mail-Adresse des Bürgers.
     *
     * @return die E-Mail-Adresse
     */
    public String getEmail() {
        return email;
    }

    /**
     * Setzt die E-Mail-Adresse des Bürgers.
     *
     * @param email die E-Mail-Adresse
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Liefert das Passwort des Bürgers.
     *
     * @return das Passwort
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setzt das Passwort des Bürgers.
     *
     * @param password das Passwort
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Erstellt ein neues {@link CitizenDto} mit allen erforderlichen Feldern.
     *
     * @param userId eindeutige Benutzer-ID
     * @param vorname Vorname des Bürgers
     * @param nachname Nachname des Bürgers
     * @param email E-Mail-Adresse des Bürgers
     * @param password Passwort des Bürgers
     */
    public CitizenDto(String userId, String vorname, String nachname, String email, String password) {
        this.userId = userId;
        this.vorname = vorname;
        this.nachname = nachname;
        this.email = email;
        this.password = password;
    }
}
