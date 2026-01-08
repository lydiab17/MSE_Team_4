package com.evote.app.citizen_management.application.services;

import com.evote.app.citizen_management.application.CitizenAggregator;
import com.evote.app.citizen_management.application.dto.CitizenDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.evote.app.citizen_management.application.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.events.DomainEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenProjector;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final EventStore eventRepository;
    private final CitizenProjector citizenProjector;
    private final CitizenAggregator citizenAggregator;


    public CitizenService(CitizenRepository citizenRepository, CitizenAggregator citizenAggregator, CitizenProjector citizenProjector, EventStore eventStore) {
        this.citizenRepository = citizenRepository;
        this.eventRepository = eventStore;
        this.citizenProjector = citizenProjector;
        this.citizenAggregator = citizenAggregator;
    }

    /**
     * Use Case: Registrierung
     */
    public Citizen registerCitizen(CitizenRegistrationRequestDto registrationInput) throws UserAlreadyExistsException {
        CitizenRegistrationCommand citizenRegistrationCommand = new CitizenRegistrationCommand(registrationInput.firstName(), registrationInput.lastName(), registrationInput.email(), registrationInput.password());
        DomainEvent event = this.citizenAggregator.handle(citizenRegistrationCommand).get(0);
        return this.citizenProjector.apply((CitizenCreatedEvent) event);
    }

    /**
     * Use Case: Login
     */
    public boolean loginCitizen(String email, String passwort) {
        Email emailObj = new Email(email);

        return citizenRepository.findByEmail(emailObj)
                .map(c -> c.getPassword().password().equals(passwort))
                .orElse(false);
    }

    public CitizenDto getCurrentLoggedInCitizen() {
        String mail = (String) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Citizen citizen = this.citizenRepository.findByEmail(new Email(mail)).orElseThrow();
        return new CitizenDto(citizen.getCitizenID().toString(), citizen.getName().firstName(), citizen.getName().lastName(), citizen.getEmail().email(), citizen.getPassword().password());
    }

    private void project() {
        this.citizenProjector.project(this.eventRepository.getEvents());
        this.eventRepository.clear();
    }

}

