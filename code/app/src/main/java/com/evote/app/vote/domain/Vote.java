package com.evote.app.vote.domain;

import com.evote.app.vote.domain.valueobjects.PseudonymToken;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Einfaches immutable Vote-Aggregate (minimal).
 */
public final class Vote {

  private final String id;
  private final int votingId;
  private final String optionId;
  private final PseudonymToken pseudonym;
  private final Instant submittedAt;

  private Vote(String id,
               int votingId,
               String optionId,
               PseudonymToken pseudonym,
               Instant submittedAt) {
    this.id = Objects.requireNonNull(id, "id");
    this.votingId = votingId; // primitive, kann nicht null sein
    this.optionId = Objects.requireNonNull(optionId, "optionId");
    this.pseudonym = Objects.requireNonNull(pseudonym, "pseudonym");
    this.submittedAt = Objects.requireNonNull(submittedAt, "submittedAt");
  }

  /**
   * Fabrikmethode zum Erzeugen einer neuen Stimme.
   *
   * @param votingId   ID der Abstimmung
   * @param optionId   gewählte Option
   * @param pseudonym  Pseudonym des Wählers
   * @return neues {@code Vote}-Objekt
   */
  public static Vote createNew(int votingId, String optionId, PseudonymToken pseudonym) {
    return new Vote(
            UUID.randomUUID().toString(),
            votingId,
            optionId,
            pseudonym,
            Instant.now()
    );
  }

  // Getter

  public String getId() {
    return id;
  }

  public int getVotingId() {
    return votingId;
  }

  public String getOptionId() {
    return optionId;
  }

  public PseudonymToken getPseudonym() {
    return pseudonym;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }
}
