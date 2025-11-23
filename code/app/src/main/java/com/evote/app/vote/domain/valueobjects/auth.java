package com.evote.app.vote.domain.valueobjects;

public class auth {
    public static class VerificationStatus {
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
