package com.evote.app.votingmanagement.domain.valueobjects;

public class port {

  public static interface AuthPort {
    auth.VerificationStatus verify(String token);
  }
}
