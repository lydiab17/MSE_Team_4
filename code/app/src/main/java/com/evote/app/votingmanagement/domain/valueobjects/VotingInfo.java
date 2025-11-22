package com.evote.app.votingmanagement.domain.valueobjects;

/**
 * Repräsentiert den Beschreibungstext ({@code info}) einer Abstimmung.
 *
 * <p>Fachliche Regeln:
 * <ul>
 *     <li>darf nicht {@code null} sein</li>
 *     <li>darf nach dem Trimmen nicht leer sein</li>
 *     <li>Länge (nach Trim): mindestens 30, höchstens 1000 Zeichen</li>
 *     <li>muss mit einem Großbuchstaben beginnen</li>
 *     <li>Zeilenumbrüche und sonstige Zeichen im Rest sind erlaubt</li>
 * </ul>
 *
 * <p>Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public class VotingInfo {

  private static final int MIN_LENGTH = 30;
  private static final int MAX_LENGTH = 1000;

  private final String value;

  /**
   * Erstellt ein neues {@code VotingInfo}-Objekt und validiert den Beschreibungstext.
   *
   * @param raw der ungeprüfte Beschreibungstext
   * @throws IllegalArgumentException wenn der Text die fachlichen Regeln verletzt
   */
  public VotingInfo(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Info darf nicht null sein");
    }

    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Info darf nicht leer sein");
    }

    int len = trimmed.length();
    if (len < MIN_LENGTH || len > MAX_LENGTH) {
      throw new IllegalArgumentException("Info muss zwischen 30 und 1000 Zeichen lang sein");
    }

    // Nicht mit Regex geprüft, da es so einfacher ist
    char first = trimmed.charAt(0);
    if (!Character.isUpperCase(first)) {
      throw new IllegalArgumentException("Info muss mit Großbuchstaben beginnen");
    }

    // Rest darf alles, inkl. Zeilenumbruch → kein weiterer Regex nötig
    this.value = raw;
  }

  public String getValue() {
    return value;
  }
}
