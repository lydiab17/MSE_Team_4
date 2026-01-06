package citizen_management.domain.events;

import com.evote.app.citizen_management.aggregator.CitizenAggregator;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationResponseDto;
import com.evote.app.citizen_management.application.services.CitizenService;
import com.evote.app.citizen_management.domain.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenProjector;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import com.evote.app.citizen_management.infrastructure.repositories.InMemoryCitizenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

@SpringJUnitConfig(classes = { CitizenService.class,
        CitizenAggregator.class,
        CitizenProjector.class,
        EventStore.class,
        InMemoryCitizenRepository.class })
public class CitizenCommandEventTest {

    @Autowired
    private CitizenAggregator citizenAggregator;

    @Autowired
    private EventStore eventStore;

    @Autowired
    private CitizenProjector citizenProjector;

    @Autowired
    private CitizenRepository citizenRepository;

    @AfterEach()
    void cleanUp() {
        eventStore.clear();
        citizenRepository.clear();
    }

    @Test
    void testRegistrationEventFlow() throws UserAlreadyExistsException {
        // 1. Command creating
        CitizenRegistrationCommand citizenRegistrationCommand = new CitizenRegistrationCommand("Max", "Mustermann", "max.mustermann@test.de", "testtest1234");

        // 2. Aggregator handles the command and put the successfull events in the event store repos
        List<DomainEvent> domainEvents = citizenAggregator.handle(citizenRegistrationCommand);

        // 3. Project all events; writing the read/query repository (CitizenRepository)
        citizenProjector.project(domainEvents);

        // 4. Receive the objects for showing the user. CitizenRegistrationResponseDto is the needed projection of the registration (for the UI)
        Citizen citizen = citizenRepository.findByEmail(new Email("max.mustermann@test.de")).get();
        CitizenRegistrationResponseDto citizenRegistrationResponseDto = CitizenRegistrationResponseDto.fromDomain(citizen);

        // assert
        Assertions.assertEquals("Max", citizenRegistrationResponseDto.firstName());
        Assertions.assertEquals("Mustermann", citizenRegistrationResponseDto.lastName());
    }

}
