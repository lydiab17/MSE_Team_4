package com.evote.app.citizen_management.domain.events;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

// TODO
public abstract class DomainEvent {
    public final UUID id = UUID.randomUUID();
    private final Instant createdAt = Instant.now();

    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }

}
