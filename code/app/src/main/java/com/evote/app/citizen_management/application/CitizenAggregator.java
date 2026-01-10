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
 * Dieses Aggregat kann einen Command akzeptieren (und ausführen) oder ablehnen.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@Service
public class CitizenAggregator {

  private final CitizenRepository citizenRepository;
  private final EventStore eventStore;

  public CitizenAggregator(CitizenRepository citizenRepository, EventStore eventStore) {
    this.citizenRepository = citizenRepository;
    this.eventStore = eventStore;
  }

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
