package com.evote.app.citizen_management.infrastructure.adapters;

import com.evote.app.citizen_management.application.services.TokenService;
import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.port.AuthPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adapter zur Authentifizierung von Bürgern.
 * <p>
 * Diese Klasse stellt eine Implementierung des {@link AuthPort} dar und
 * fungiert als Brücke zwischen dem Voting-Management-Kontext und dem
 * Citizen-Management-Kontext.
 * </p>
 * <p>
 * Der Adapter validiert Authentifizierungs-Tokens und erzeugt daraus
 * pseudonymisierte Tokens, um die Identität eines Bürgers zu schützen.
 * </p>
 */
@Component
public class CitizenAuthAdapter implements AuthPort {

    /**
     * Service zur Validierung und Pseudonymisierung von Tokens.
     */
    private final TokenService tokenService;

    /**
     * Erstellt einen neuen {@code CitizenAuthAdapter}.
     *
     * @param tokenService Service zur Token-Verarbeitung
     */
    public CitizenAuthAdapter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    /**
     * Verifiziert ein Authentifizierungs-Token und liefert ein Pseudonym-Token zurück.
     * <p>
     * Das übergebene {@link AuthToken} wird validiert. Ist das Token ungültig
     * oder kann keine Bürger-ID ermittelt werden, wird ein leeres {@link Optional}
     * zurückgegeben.
     * </p>
     * <p>
     * Bei erfolgreicher Validierung wird die ermittelte Bürger-ID pseudonymisiert,
     * um eine Weiterverarbeitung ohne Offenlegung der tatsächlichen Identität
     * zu ermöglichen.
     * </p>
     *
     * @param token das zu prüfende Authentifizierungs-Token
     * @return ein {@link Optional} mit {@link PseudonymToken} bei erfolgreicher
     *         Verifikation, ansonsten {@link Optional#empty()}
     */
    @Override
    public Optional<PseudonymToken> verifyAndGetPseudonym(AuthToken token) {
        String citizenId = TokenService.validateToken(token.value()); // String oder null
        if (citizenId == null || citizenId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(tokenService.pseudonymize(citizenId));
    }
}
