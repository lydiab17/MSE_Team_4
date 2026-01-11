package com.evote.app.citizen_management.application;

import com.evote.app.citizen_management.application.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.application.dto.CitizenDto;
import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Aggregator für das Citizen-Domain-Modell.
 *
 * <p>Der Aggregator ist für die Verarbeitung von Commands zuständig und stellt sicher, dass alle
 * fachlichen Regeln (Invarianten) eingehalten werden. Er entscheidet, ob ein Command ausgeführt
 * werden darf oder abgelehnt wird.
 *
 * <p>Bei erfolgreicher Verarbeitung eines Commands erzeugt der Aggregator entsprechende {@link
 * DomainEvent}s und speichert diese im {@link EventStore}. Der Aggregator selbst verändert dabei
 * keinen persistierten Zustand direkt.
 */
@Service
public class CitizenAggregator {

  /** Repository zum Prüfen bestehender Citizen-Daten (z. B. zur E-Mail-Eindeutigkeit). */
  private final CitizenRepository citizenRepository;

  /** Event Store zur dauerhaften Speicherung von Domain Events. */
  private final EventStore eventStore;

  /**
   * Erstellt einen neuen {@code CitizenAggregator}.
   *
   * @param citizenRepository Repository für Citizen-Daten
   * @param eventStore Event Store zur Speicherung der erzeugten Domain Events
   */
  public CitizenAggregator(CitizenRepository citizenRepository, EventStore eventStore) {
    this.citizenRepository = citizenRepository;
    this.eventStore = eventStore;
  }

  /**
   * Verarbeitet den Command zur Registrierung eines Bürgers.
   *
   * <p>Der Aggregator prüft zunächst, ob der Command fachlich zulässig ist (z. B. ob die
   * E-Mail-Adresse bereits existiert). Ist der Command gültig, wird ein {@link CitizenCreatedEvent}
   * erzeugt und im {@link EventStore} gespeichert.
   *
   * @param command Command mit den Daten zur Bürgerregistrierung
   * @return Liste der durch den Command erzeugten {@link DomainEvent}s
   * @throws UserAlreadyExistsException wenn bereits ein Bürger mit der angegebenen E-Mail existiert
   */
  public List<DomainEvent> handle(CitizenRegistrationCommand command)
      throws UserAlreadyExistsException {
    // Check if command can be executed
    Citizen citizen =
        Citizen.create(
            new Name(command.vorname(), command.nachname()),
            new Email(command.email()),
            new Password(command.password()));
    if (citizenRepository.findByEmail(citizen.getEmail()).isPresent()) {
      throw new UserAlreadyExistsException(command.email());
    }

    // Command can be executed, will be added as event
    CitizenCreatedEvent citizenCreatedEvent =
        new CitizenCreatedEvent(
            new CitizenDto(
                null, command.vorname(), command.nachname(), command.email(), command.password()));
    eventStore.addEvent(UUID.randomUUID().toString(), citizenCreatedEvent);
    return List.of(citizenCreatedEvent);
  }
}
