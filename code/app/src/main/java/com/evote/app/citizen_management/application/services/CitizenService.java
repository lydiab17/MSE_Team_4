package com.evote.app.citizen_management.application.services;

import com.evote.app.citizen_management.aggregator.CitizenAggregator;
import com.evote.app.citizen_management.application.dto.CitizenDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
import com.evote.app.citizen_management.domain.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.domain.events.CitizenCreatedEvent;
import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.exceptions.UserAlreadyExistsException;
import com.evote.app.citizen_management.infrastructure.CitizenProjector;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.EventStore;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;
    private final EventStore eventRepository;
    private final CitizenProjector citizenProjector;
    private final CitizenAggregator citizenAggregator;

    private Citizen logincitizen;

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
        return this.citizenProjector.apply((CitizenCreatedEvent) this.citizenAggregator.handle(citizenRegistrationCommand).get(0));
    }

    /**
     * Use Case: Login
     */
    public boolean loginCitizen(String email, String passwort) {
        Email email1 = new Email(email);
        Optional<Citizen> citizenOpt = citizenRepository.findByEmail(email1);

        System.out.println("Hier: " + citizenOpt);

        // Prüfen ob Citizen existiert
        if (citizenOpt.isPresent()) {
            Citizen c = citizenOpt.get();

            // Passwort vergleichen (anpassen, falls Passwort gehasht ist!)
            if (c.getPassword().password().equals(passwort)) {
                return true;
            }
        }

        return false;
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

