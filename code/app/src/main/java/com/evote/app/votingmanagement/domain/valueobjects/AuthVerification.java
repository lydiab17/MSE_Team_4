package com.evote.app.votingmanagement.domain.valueobjects;

/**
 * Value-Objects rund um die Verifikation eines Authentifizierungsnachweises.
 */
public final class AuthVerification {

  private AuthVerification() {
    // Utility-Klasse / Namespace
  }

  /**
   * Ergebnis einer Verifikation.
   *
   * <p>Wenn {@code verified} {@code false} ist, kann {@code pseudonym} {@code null} oder leer sein.
   */
  public static final class VerificationStatus {

    private final boolean verified;
    private final String pseudonym; // null oder leer, falls nicht verifiziert

    public VerificationStatus(boolean verified, String pseudonym) {
      this.verified = verified;
      this.pseudonym = pseudonym;
    }

    public boolean isVerified() {
      return verified;
    }

    public String getPseudonym() {
      return pseudonym;
    }
  }
}
