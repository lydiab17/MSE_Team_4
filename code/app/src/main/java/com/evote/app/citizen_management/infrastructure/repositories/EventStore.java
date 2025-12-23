package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dies ist ein Repository zur Speicherung der Domainevents.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@Service
public class EventStore {
    private Map<String, List<DomainEvent>> store = new HashMap<>();

    public void addEvent(String key, DomainEvent event) {
        List<DomainEvent> events = this.store.getOrDefault(key, new ArrayList<>());
        events.add(event);
        this.store.put(key, events);
    }

    public List<DomainEvent> getEvents() {
        return this.store.values().stream().flatMap(List::stream).toList();
    }

    public void clear() {
        this.store.clear();
    }
}
