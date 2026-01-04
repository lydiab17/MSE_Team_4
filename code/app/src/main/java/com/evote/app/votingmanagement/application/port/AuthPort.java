package com.evote.app.votingmanagement.application.port;

import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import java.util.Optional;

/**
 * Port zur Authentifizierung und zum Ableiten eines Pseudonyms aus einem Auth-Token.
 */
public interface AuthPort {

  /**
   * Verifiziert das gegebene Auth-Token und liefert bei Erfolg das zugehörige Pseudonym.
   *
   * @param token das zu verifizierende Auth-Token
   * @return das Pseudonym-Token, falls die Verifikation erfolgreich ist, sonst
   * {@link Optional#empty()}
   */
  Optional<PseudonymToken> verifyAndGetPseudonym(AuthToken token);
}
