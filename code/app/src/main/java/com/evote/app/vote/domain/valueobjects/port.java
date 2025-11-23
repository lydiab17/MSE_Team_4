package com.evote.app.vote.domain.valueobjects;

public class port {

  public static interface AuthPort {
    auth.VerificationStatus verify(String token);
  }
}
