package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Projector zur Umsetzung von Domain Events in persistierbaren Zustand.
 *
 * <p>Der Projector verarbeitet {@link DomainEvent}s, die zuvor vom Aggregator erzeugt wurden, und
 * projiziert diese in ein konkretes Read Model.
 *
 * <p>Er enthält keine fachlichen Entscheidungen, sondern setzt ausschließlich das um, was durch
 * Domain Events bereits entschieden wurde.
 */
@Service
public class CitizenProjector {

  /** Repository zur Persistierung und zum Laden von Citizen-Entitäten. */
  private final CitizenRepository citizenRepository;

  /**
   * Erstellt einen neuen {@code CitizenProjector}.
   *
   * @param citizenRepository Repository zur Speicherung von Citizen-Daten
   */
  public CitizenProjector(CitizenRepository citizenRepository) {
    this.citizenRepository = citizenRepository;
  }

  /**
   * Verarbeitet eine Liste von Domain Events und projiziert alle unterstützten Event-Typen in den
   * aktuellen Systemzustand. Koordination einer Menge von Events.
   *
   * <p>Nicht unterstützte Events werden ignoriert. Für jedes bekannte Event wird die entsprechende
   * {@code apply}-Methode aufgerufen.
   *
   * @param events Liste von Domain Events, die projiziert werden sollen
   */
  public void project(List<DomainEvent> events) {
    events.stream()
        .filter(event -> event instanceof CitizenCreatedEvent)
        .forEach(
            event -> {
              apply((CitizenCreatedEvent) event);
            });
  }

  /**
   * Wendet ein {@link CitizenCreatedEvent} auf das System an. (konkrete Umsetzung eines Events)
   *
   * <p>Aus den im Event enthaltenen Daten wird ein neues {@link Citizen} erzeugt und anschließend
   * im {@link CitizenRepository} persistiert.
   *
   * @param event Event, das die Erstellung eines neuen Bürgers beschreibt
   * @return der neu erstellte und gespeicherte {@link Citizen}
   */
  public Citizen apply(CitizenCreatedEvent event) {
    Citizen citizen =
        Citizen.create(
            new Name(event.getCitizenDto().getVorname(), event.getCitizenDto().getNachname()),
            new Email(event.getCitizenDto().getEmail()),
            new Password(event.getCitizenDto().getPassword()));

    citizenRepository.save(citizen);
    return citizen;
  }
}
