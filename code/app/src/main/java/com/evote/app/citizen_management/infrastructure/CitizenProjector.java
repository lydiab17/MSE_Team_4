package com.evote.app.citizen_management.infrastructure;

import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CitizenProjector {

    private final CitizenRepository citizenRepository;

    public CitizenProjector(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }


    public void project(List<DomainEvent> events) {
        events.stream().filter(event -> event instanceof CitizenCreatedEvent).forEach(event -> {
            apply((CitizenCreatedEvent) event);
        });
    }

    public Citizen apply(CitizenCreatedEvent event) {
        Citizen citizen = Citizen.create(
                new Name(event.getCitizenDto().getVorname(), event.getCitizenDto().getNachname()),
                new Email(event.getCitizenDto().getEmail()),
                new Password(event.getCitizenDto().getPassword()));

        citizenRepository.save(citizen);
        return citizen;
    }
}
