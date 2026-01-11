package citizen_management.domain.events;

import com.evote.app.citizen_management.application.CitizenAggregator;
import com.evote.app.citizen_management.application.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationResponseDto;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenProjector;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import com.evote.app.citizen_management.infrastructure.repositories.InMemoryCitizenRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Integration-/Component-Test.
 *
 * Ziel:
 * - Registrierungs-Command wird vom Aggregator verarbeitet
 * - dabei entstehende DomainEvents werden im EventStore abgelegt
 * - anschließend werden die Events in ein Read-Model (CitizenRepository) projiziert
 * - das Read-Model wird abgefragt und in ein Response-DTO gemappt
 *
 * Technisch:
 * - SpringJUnitConfig erstellt einen kleinen Spring Context nur mit den benötigten Beans.
 */
@SpringJUnitConfig(
    classes = {
      CitizenService.class,
      CitizenAggregator.class,
      CitizenProjector.class,
      EventStore.class,
      InMemoryCitizenRepository.class
    })
class CitizenCommandEventTest {

  @Autowired private CitizenAggregator citizenAggregator;

  @Autowired private EventStore eventStore;

  @Autowired private CitizenProjector citizenProjector;

  @Autowired private CitizenRepository citizenRepository;

  @AfterEach()
  void cleanUp() {
    eventStore.clear();
    citizenRepository.clear();
  }

  // Happy Path Test
  @Test
  void testRegistrationEventFlow() throws UserAlreadyExistsException {
      // 1. Command erstellen: beschreibt die gewünschte Aktion (Citizen registrieren)
    CitizenRegistrationCommand citizenRegistrationCommand =
        new CitizenRegistrationCommand(
            "Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

    // 2. Aggregator verarbeitet den Command und speichert die Events im Event Store
    List<DomainEvent> domainEvents = citizenAggregator.handle(citizenRegistrationCommand);

    // 3. Projection: Events werden auf die Read-Seite projiziert
    citizenProjector.project(domainEvents);

      // 4. Query: Read-Model abfragen und in ein UI-taugliches Response-DTO umwandeln.
    Citizen citizen = citizenRepository.findByEmail(new Email("max.mustermann@test.de")).get();
    CitizenRegistrationResponseDto citizenRegistrationResponseDto =
        CitizenRegistrationResponseDto.fromDomain(citizen);

      // 5. Assert: Prüfen, ob die Projektion die erwarteten Werte enthält.
    Assertions.assertEquals("Max", citizenRegistrationResponseDto.firstName());
    Assertions.assertEquals("Mustermann", citizenRegistrationResponseDto.lastName());
  }
}
