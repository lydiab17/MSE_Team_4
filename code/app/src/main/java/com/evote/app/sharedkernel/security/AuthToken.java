package com.evote.app.sharedkernel.security;

import java.util.Objects;

/**
 * Wrappt ein Auth-Token (z.B. JWT) als Value Object.
 */
public record AuthToken(String value) {

  /**
   * Erstellt ein {@link AuthToken} und validiert den Token-String.
   *
   * @param value Token-Wert (z.B. JWT); darf nicht {@code null} oder leer sein
   * @throws NullPointerException     wenn {@code value} {@code null} ist
   * @throws IllegalArgumentException wenn {@code value} leer/blank ist
   */
  public AuthToken {
    Objects.requireNonNull(value, "AuthToken darf nicht null sein");
    if (value.isBlank()) {
      throw new IllegalArgumentException("AuthToken darf nicht leer sein");
    }
  }
}
