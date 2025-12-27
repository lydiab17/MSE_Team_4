package com.evote.app.votingmanagement.events;

import java.time.Instant;

/**
 * Event, das nach dem Persistieren eines Votes veröffentlicht werden kann.
 */
public class VotePersistedEvent {

  public final String voteId;
  public final String votingId;
  public final Instant recordedAt;
  public final String storageHash; // optional, can be null

  /**
   * Erstellt ein {@link VotePersistedEvent}.
   *
   * @param voteId      ID des gespeicherten Votes
   * @param votingId    ID der zugehörigen Abstimmung
   * @param recordedAt  Zeitpunkt der Aufzeichnung
   * @param storageHash optionaler Hash/Verweis auf die Speicherung (kann {@code null} sein)
   */
  public VotePersistedEvent(
          String voteId,
          String votingId,
          Instant recordedAt,
          String storageHash
  ) {
    this.voteId = voteId;
    this.votingId = votingId;
    this.recordedAt = recordedAt;
    this.storageHash = storageHash;
  }
}
