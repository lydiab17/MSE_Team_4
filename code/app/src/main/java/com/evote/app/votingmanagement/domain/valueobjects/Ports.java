package com.evote.app.votingmanagement.domain.valueobjects;

/**
 * Sammelklasse für Port-Definitionen in diesem Package.
 */
public final class Ports {

  private Ports() {
    // Utility-Klasse / Namespace
  }

  /**
   * Port zur Verifikation eines Tokens und zum Ableiten eines Verifikationsstatus.
   */
  public interface AuthPort {

    /**
     * Verifiziert das gegebene Token.
     *
     * @param token das zu verifizierende Token
     * @return Verifikationsstatus inkl. Pseudonym (falls verifiziert)
     */
    AuthVerification.VerificationStatus verify(String token);
  }
}
