package com.evote.app.votingmanagement.application.port;

import com.evote.app.sharedkernel.security.AuthToken;
import com.evote.app.sharedkernel.security.PseudonymToken;
import java.util.Optional;

public interface AuthPort {
    Optional<PseudonymToken> verifyAndGetPseudonym(AuthToken token);
}
