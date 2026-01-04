package com.evote.app.votingmanagement.ui.api;

import java.util.Optional;

/**
 * Einfacher, globaler Token-Speicher für den Prototyp.
 *
 * <p>Speichert ein JWT im Speicher der laufenden Anwendung. Nicht für produktive Nutzung gedacht.
 */
public class TokenStore {

  private static volatile String jwt; // simple für Prototyp

  private TokenStore() {
  }

  public static void setJwt(String token) {
    jwt = token;
  }

  public static Optional<String> getJwt() {
    return Optional.ofNullable(jwt);
  }

  public static void clear() {
    jwt = null;
  }
}
