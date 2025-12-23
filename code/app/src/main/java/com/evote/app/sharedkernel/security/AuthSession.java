package com.evote.app.sharedkernel.security;

import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Speichert den JWT im laufenden JavaFX-Client (in-memory).
 * Reicht für Prototyp / Demo.
 */
@Component
public class AuthSession {

  private String token; // "raw" JWT ohne "Bearer "

  public void setToken(String token) {
    this.token = token;
  }

  public Optional<String> token() {
    return Optional.ofNullable(token);
  }

  public void clear() {
    token = null;
  }
}
