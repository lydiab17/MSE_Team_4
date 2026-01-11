package com.evote.app.votingmanagement.domain.valueobjects;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Repräsentiert den Text einer einzelnen Wahl-Option (z.B. "Ja", "Nein").
 *
 * <p>Fachliche Regeln:
 *
 * <ul>
 *   <li>darf nicht {@code null} sein
 *   <li>darf nicht nur aus Leerzeichen bestehen
 *   <li>erlaubte Zeichen: Buchstaben, Ziffern und Leerzeichen
 *   <li>Sonderzeichen wie {@code ! ? , .} sind nicht erlaubt
 * </ul>
 *
 * <p>Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public final class OptionLabel {

  // \p{L}  -> irgendein Buchstabe (Letter, auch Umlaute, andere Sprachen etc.)
  // \p{Nd} -> Dezimalziffer (Number, decimal digit, 0–9)
  private static final String PATTERN = "^[\\p{L}\\p{Nd} ]+$";

  private final String value;

  private static final List<Rule> RULES =
      List.of(
          new Rule(s -> !s.isEmpty(), "Option darf nicht leer sein"),
          new Rule(
              OptionLabel::matchesPattern,
              "Option darf nur Buchstaben, Ziffern und Leerzeichen enthalten"));

  /**
   * Erstellt ein neues {@code OptionLabel}.
   *
   * @param raw der ungeprüfte Text der Option
   * @throws IllegalArgumentException wenn der Text ungültig ist
   */
  public OptionLabel(String raw) {
    Objects.requireNonNull(raw, "Option darf nicht null sein");

    String trimmed = raw.trim();

    RULES.stream()
        .filter(rule -> !rule.predicate().test(trimmed))
        .findFirst()
        .ifPresent(
            rule -> {
              throw new IllegalArgumentException(rule.message());
            });

    this.value = trimmed;
  }

  private static boolean matchesPattern(String s) {
    return s.matches(PATTERN);
  }

  private record Rule(Predicate<String> predicate, String message) {}

  public String getValue() {
    return value;
  }
}
