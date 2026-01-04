package com.evote.app.votingmanagement.domain.valueobjects;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Repräsentiert den Namen/Titel einer Abstimmung.
 *
 * <p>Fachliche Regeln:
 * <ul>
 *     <li>darf nicht {@code null} sein</li>
 *     <li>darf nach dem Trimmen nicht leer sein</li>
 *     <li>Länge (nach Trim): mindestens 10, höchstens 100 Zeichen</li>
 *     <li>muss mit einem Großbuchstaben beginnen (inkl. Ä/Ö/Ü usw.)</li>
 *     <li>erlaubte Zeichen: Buchstaben, Ziffern und Leerzeichen</li>
 *     <li>keine Sonderzeichen wie {@code ! ? , .} usw.</li>
 * </ul>
 *
 * <p>Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public final class VotingName {

  private final String value;

  private static final int MIN_LENGTH = 10;
  private static final int MAX_LENGTH = 100;

  // \p{Lu} -> ein Großbuchstabe (Unicode Uppercase Letter, z.B. A–Z, Ä, Ö, Ü)
  // \p{L}  -> irgendein Buchstabe (Letter, auch Umlaute, andere Sprachen etc.)
  // \p{Nd} -> Dezimalziffer (Number, decimal digit, 0–9)
  private static final String PATTERN = "^[\\p{Lu}][\\p{L}\\p{Nd} ]*$";

  /**
   * Regeln als Daten (funktionaler Stil): Jede Regel ist eine Predicate+Message-Kombination.
   * Fail-fast: wirft beim ersten Verstoß eine IllegalArgumentException.
   */
  private static final List<Rule> RULES = List.of(
          new Rule(s -> !s.isEmpty(), "Name darf nicht leer sein"),
          new Rule(VotingName::isValidLength, "Name muss zwischen 10 und 100 Zeichen lang sein"),
          new Rule(VotingName::matchesPattern,
                  "Name muss mit Großbuchstaben beginnen und darf nur Buchstaben, Ziffern und Leerzeichen enthalten")
  );

  /**
   * Erstellt ein neues {@code VotingName}-Objekt und validiert den Namen.
   *
   * @param raw der ungeprüfte Name
   * @throws IllegalArgumentException wenn der Name ungültig ist
   */
  public VotingName(String raw) {
    Objects.requireNonNull(raw, "Name darf nicht null sein");

    String trimmed = raw.trim();

    RULES.stream()
            .filter(rule -> !rule.predicate().test(trimmed))
            .findFirst()
            .ifPresent(rule -> {
              throw new IllegalArgumentException(rule.message());
            });

    this.value = trimmed;
  }

  private static boolean isValidLength(String s) {
    int len = s.length();
    return len >= MIN_LENGTH && len <= MAX_LENGTH;
  }

  private static boolean matchesPattern(String s) {
    return s.matches(PATTERN);
  }

  private record Rule(Predicate<String> predicate, String message) {}

  public String getValue() {
    return value;
  }
}
