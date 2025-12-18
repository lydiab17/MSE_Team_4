package com.evote.app.votingmanagement.application.port;

import com.evote.app.sharedkernel.AuthToken;
import com.evote.app.sharedkernel.PseudonymToken;
import java.util.Optional;

public interface AuthPort {
    Optional<PseudonymToken> verifyAndGetPseudonym(AuthToken token);
}
