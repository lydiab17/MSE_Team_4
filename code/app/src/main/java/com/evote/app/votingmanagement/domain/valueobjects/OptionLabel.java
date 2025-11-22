package com.evote.app.votingmanagement.domain.valueobjects;

/**
 * Repräsentiert den Text einer einzelnen Wahl-Option (z.B. "Ja", "Nein").
 *
 * <p>Fachliche Regeln:
 * <ul>
 *     <li>darf nicht {@code null} sein</li>
 *     <li>darf nicht nur aus Leerzeichen bestehen</li>
 *     <li>erlaubte Zeichen: Buchstaben, Ziffern und Leerzeichen</li>
 *     <li>Sonderzeichen wie {@code ! ? , .} sind nicht erlaubt</li>
 * </ul>
 *
 * <p>Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public class OptionLabel {

  // \\p{Lu} -> ein Großbuchstabe (Unicode Uppercase Letter, z.B. A–Z, Ä, Ö, Ü)
  // \\p{Nd}-> Dezimalziffer (Number, decimal digit, 0–9)
  private static final String PATTERN = "^[\\p{L}\\p{Nd} ]+$";

  private final String value;

  /**
   * Erstellt ein neues {@code OptionLabel}.
   *
   * @param raw der ungeprüfte Text der Option
   * @throws IllegalArgumentException wenn der Text ungültig ist
   */
  public OptionLabel(String raw) {
    if (raw == null) {
      throw new IllegalArgumentException("Option darf nicht null sein");
    }

    String trimmed = raw.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Option darf nicht leer sein");
    }

    if (!trimmed.matches(PATTERN)) {
      throw new IllegalArgumentException(
              "Option darf nur Buchstaben, Ziffern und Leerzeichen enthalten");
    }

    this.value = trimmed;
  }

  public String getValue() {
    return value;
  }
}
