package com.evote.app.vote.domain;

import com.evote.app.vote.domain.auth.VerificationStatus;
public class port {

    public static interface AuthPort {
        auth.VerificationStatus verify(String token);
    }
}
