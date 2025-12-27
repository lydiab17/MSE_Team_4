package com.evote.app.citizen_management.infrastructure.adapters;

import com.evote.app.citizen_management.application.services.TokenService;
import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import com.evote.app.votingmanagement.application.port.AuthPort;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CitizenAuthAdapter implements AuthPort {

    private final TokenService tokenService;

    public CitizenAuthAdapter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public Optional<PseudonymToken> verifyAndGetPseudonym(AuthToken token) {
        String citizenId = TokenService.validateToken(token.value()); // String oder null
        if (citizenId == null || citizenId.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(tokenService.pseudonymize(citizenId));
    }
}
