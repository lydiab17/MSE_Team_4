package com.evote.app.citizen_management.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Basisklasse für alle Domain Events im System.
 *
 * <p>Ein {@code DomainEvent} repräsentiert ein fachliches Ereignis, das innerhalb der Domäne
 * aufgetreten ist. Jedes Event besitzt eine eindeutige ID sowie einen Zeitstempel, der den
 * Erstellungszeitpunkt des Events angibt.
 */
public abstract class DomainEvent {

  /**
   * Eindeutige Identifikationsnummer des Domain Events.
   *
   * <p>Die ID wird beim Erzeugen des Events automatisch generiert.
   */
  public final UUID id = UUID.randomUUID();

  /**
   * Zeitstempel, der den Zeitpunkt der Erstellung des Events festhält.
   *
   * <p>Der Wert wird beim Erzeugen des Events gesetzt und bleibt unveränderlich.
   */
  private final Instant createdAt = Instant.now();

  /**
   * Liefert die eindeutige ID dieses Domain Events.
   *
   * @return die {@link UUID} des Events
   */
  public UUID getId() {
    return id;
  }

  /**
   * Liefert den Erstellungszeitpunkt dieses Domain Events.
   *
   * @return der Zeitpunkt der Event-Erstellung als {@link Instant}
   */
  public Instant getCreatedAt() {
    return createdAt;
  }
}
