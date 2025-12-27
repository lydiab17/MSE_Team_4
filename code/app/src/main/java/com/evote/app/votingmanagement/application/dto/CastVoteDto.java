package com.evote.app.votingmanagement.application.dto;

/**
 * Einfaches DTO für CastVote.
 */
public final class CastVoteDto {
  public final String authToken;
  public final int votingId;
  public final String optionId;

  /**
   * Erstellt ein neues DTO für die Stimmabgabe.
   *
   * @param authToken Authentifizierungs-Token (z. B. JWT)
   * @param votingId  ID der Abstimmung
   * @param optionId  gewählte Option
   */
  public CastVoteDto(String authToken, int votingId, String optionId) {
    this.authToken = authToken;
    this.votingId = votingId;
    this.optionId = optionId;
  }
}
