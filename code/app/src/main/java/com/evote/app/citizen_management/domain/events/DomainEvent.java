package com.evote.app.citizen_management.domain.events;

import java.util.Date;
import java.util.UUID;

// TODO
public abstract class DomainEvent {
    public final UUID id = UUID.randomUUID();
    public final Date created = new Date();
}
