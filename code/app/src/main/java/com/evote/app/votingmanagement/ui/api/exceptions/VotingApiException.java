package com.evote.app.votingmanagement.ui.api.exceptions;

public class VotingApiException extends RuntimeException {
  public VotingApiException(String message) {
    super(message);
  }

  public VotingApiException(String message, Throwable cause) {
    super(message, cause);
  }
}
