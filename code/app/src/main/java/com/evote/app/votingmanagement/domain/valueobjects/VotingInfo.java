package com.evote.app.votingmanagement.domain.valueobjects;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Repräsentiert den Beschreibungstext ({@code info}) einer Abstimmung.
 *
 * <p>Fachliche Regeln:
 * <ul>
 *   <li>darf nicht {@code null} sein</li>
 *   <li>darf nach dem Trimmen nicht leer sein</li>
 *   <li>Länge (nach Trim): mindestens 30, höchstens 1000 Zeichen</li>
 *   <li>muss mit einem Großbuchstaben beginnen</li>
 *   <li>Zeilenumbrüche und sonstige Zeichen im Rest sind erlaubt</li>
 * </ul>
 *
 * <p>Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public final class VotingInfo {

  private static final int MIN_LENGTH = 30;
  private static final int MAX_LENGTH = 1000;

  private final String value;

  private static final List<Rule> RULES = List.of(
          new Rule(s -> !s.isEmpty(), "Info darf nicht leer sein"),
          new Rule(VotingInfo::isValidLength, "Info muss zwischen 30 und 1000 Zeichen lang sein"),
          new Rule(VotingInfo::startsWithUppercase, "Info muss mit Großbuchstaben beginnen")
  );

  /**
   * Erstellt ein neues {@code VotingInfo}-Objekt und validiert den Beschreibungstext.
   *
   * @param raw der ungeprüfte Beschreibungstext
   * @throws IllegalArgumentException wenn der Text die fachlichen Regeln verletzt
   */
  public VotingInfo(String raw) {
    Objects.requireNonNull(raw, "Info darf nicht null sein");

    String trimmed = raw.trim();

    RULES.stream()
            .filter(rule -> !rule.predicate().test(trimmed))
            .findFirst()
            .ifPresent(rule -> {
              throw new IllegalArgumentException(rule.message());
            });

    // speichern konsistent zur Validierung
    this.value = trimmed;
  }

  private static boolean isValidLength(String s) {
    int len = s.length();
    return len >= MIN_LENGTH && len <= MAX_LENGTH;
  }

  private static boolean startsWithUppercase(String s) {
    // s ist hier garantiert nicht leer (Regel 1)
    return Character.isUpperCase(s.charAt(0));
  }

  private record Rule(Predicate<String> predicate, String message) {}

  public String getValue() {
    return value;
  }
}
