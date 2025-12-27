package com.evote.app.sharedkernel.security;

import java.util.Objects;

/**
 * Pseudonymer Identifier eines Bürgers (ohne Klardaten wie E-Mail).
 * Wird z.B. aus einem verifizierten JWT abgeleitet.
 */
public record PseudonymToken(String value) {

  /**
   * Erstellt ein {@link PseudonymToken} und validiert den Wert.
   *
   * @param value pseudonymer Identifier; darf nicht {@code null} oder leer sein
   * @throws NullPointerException     wenn {@code value} {@code null} ist
   * @throws IllegalArgumentException wenn {@code value} leer/blank ist
   */
  public PseudonymToken {
    Objects.requireNonNull(value, "PseudonymToken darf nicht null sein");
    if (value.isBlank()) {
      throw new IllegalArgumentException("PseudonymToken darf nicht leer sein");
    }
  }
}
