package com.evote.app.votingmanagement.events;

import java.time.Instant;

/**
 * Minimales Event-Objekt.
 *
 * <p>Pseudonym-Token als String, um Abhängigkeiten zu reduzieren.
 */
public final class VoteSubmittedEvent {

  public final String voteId;
  public final int votingId;
  public final String optionId;
  public final String pseudonymToken;
  public final Instant submittedAt;

  /**
   * Erstellt ein {@link VoteSubmittedEvent}.
   *
   * @param voteId         ID des Votes
   * @param votingId       ID der Abstimmung
   * @param optionId       ID der gewählten Option
   * @param pseudonymToken Pseudonym-Token des Wählers
   * @param submittedAt    Zeitpunkt der Stimmabgabe
   */
  public VoteSubmittedEvent(
          String voteId,
          int votingId,
          String optionId,
          String pseudonymToken,
          Instant submittedAt
  ) {
    this.voteId = voteId;
    this.votingId = votingId;
    this.optionId = optionId;
    this.pseudonymToken = pseudonymToken;
    this.submittedAt = submittedAt;
  }
}
