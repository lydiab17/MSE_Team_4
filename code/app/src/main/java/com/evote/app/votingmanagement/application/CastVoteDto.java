package com.evote.app.votingmanagement.application;

/**
 * Einfaches DTO für CastVote.
 */
public final class CastVoteDto {
  public final String authToken;
  public final int votingId;
  public final String optionId;

  public CastVoteDto(String authToken, int votingId, String optionId) {
    this.authToken = authToken;
    this.votingId = votingId;
    this.optionId = optionId;
  }
}
