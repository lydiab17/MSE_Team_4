package citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testklasse für das EventStore-Repository.
 *
 * Ziel:
 * - Überprüfen, ob Events korrekt gespeichert, gelesen und gelöscht werden
 */
class EventStoreTest {

    /**
     * Das zu testende Objekt
     */
    private EventStore eventStore;

    /**
     * Diese Methode wird vor JEDEM Test ausgeführt.
     * So starten alle Tests mit einem leeren EventStore.
     */
    @BeforeEach
    void setUp() {
        eventStore = new EventStore();
    }

    /**
     * Testet, ob ein einzelnes Event korrekt gespeichert wird.
     *
     * Erwartung:
     * - Nach dem Hinzufügen existiert genau ein Event
     * - Das gespeicherte Event ist genau das hinzugefügte Objekt
     */
    @Test
    void addEvent_shouldStoreEvent() {
        // Arrange:
        // Vorbereitung der Testdaten
        DomainEvent event = new TestDomainEvent();

        // Act:
        // Ausführen der zu testenden Methode
        eventStore.addEvent("key1", event);
        List<DomainEvent> events = eventStore.getEvents();

        // Assert:
        // Überprüfen des erwarteten Ergebnisses
        assertEquals(1, events.size(), "Es sollte genau ein Event gespeichert sein");
        assertTrue(events.contains(event), "Das gespeicherte Event muss enthalten sein");
    }

    /**
     * Testet, ob mehrere Events unter demselben Key gespeichert werden können.
     *
     * Erwartung:
     * - Beide Events werden gespeichert
     * - Die Anzahl der Events beträgt 2
     */
    @Test
    void addEvent_shouldStoreMultipleEventsWithSameKey() {
        // Arrange:
        DomainEvent event1 = new TestDomainEvent();
        DomainEvent event2 = new TestDomainEvent();

        // Act:
        eventStore.addEvent("key1", event1);
        eventStore.addEvent("key1", event2);

        // Assert:
        List<DomainEvent> events = eventStore.getEvents();
        assertEquals(2, events.size(), "Beide Events sollten gespeichert sein");
    }

    /**
     * Testet die Methode getEvents().
     *
     * Erwartung:
     * - Events aus unterschiedlichen Keys werden gemeinsam zurückgegeben
     * - Die Reihenfolge ist dabei egal
     */
    @Test
    void getEvents_shouldReturnAllEventsFromDifferentKeys() {
        // Arrange:
        DomainEvent event1 = new TestDomainEvent();
        DomainEvent event2 = new TestDomainEvent();

        // Act:
        eventStore.addEvent("key1", event1);
        eventStore.addEvent("key2", event2);

        // Assert:
        List<DomainEvent> events = eventStore.getEvents();
        assertEquals(2, events.size(), "Es sollten alle Events zurückgegeben werden");
        assertTrue(events.contains(event1), "Event von key1 fehlt");
        assertTrue(events.contains(event2), "Event von key2 fehlt");
    }

    /**
     * Testet die clear()-Methode.
     *
     * Erwartung:
     * - Nach dem Aufruf von clear() ist der EventStore leer
     */
    @Test
    void clear_shouldRemoveAllEvents() {
        // Arrange:
        eventStore.addEvent("key1", new TestDomainEvent());
        eventStore.addEvent("key2", new TestDomainEvent());

        // Act:
        eventStore.clear();

        // Assert:
        assertTrue(
                eventStore.getEvents().isEmpty(),
                "Nach clear() sollten keine Events mehr vorhanden sein"
        );
    }

    /**
     * Dummy-Implementierung von DomainEvent für Testzwecke.
     *
     * Warum?
     * - DomainEvent ist abstrakt
     * - Für diese Tests interessiert das Verhalten des EventStore,
     *   nicht der Inhalt des Events
     * - Diese Klasse ist daher bewusst leer
     */
    static class TestDomainEvent extends DomainEvent {
    }
}
