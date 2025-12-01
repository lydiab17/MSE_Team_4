package com.evote.app.citizen_management.application.services;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import com.evote.app.citizen_management.domain.valueobjects.Name;
import com.evote.app.citizen_management.domain.valueobjects.Password;
import com.evote.app.citizen_management.infrastructure.repositories.CitizenRepository;
import com.evote.app.citizen_management.infrastructure.repositories.InMemoryCitizenRepository;
import com.evote.app.votingmanagement.domain.model.VotingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;

    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    /**
     * Use Case: Registrierung
     */
    public Citizen registerCitizen(String firstname, String lastname, String email, String passwort) {

        Name name = new Name(firstname, lastname);
        Email email1 = new Email(email);
        Password passwort1 = new Password(passwort);
        Citizen citizen = Citizen.create(name, email1, passwort1);

        citizenRepository.save(citizen);

        return citizen;
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


    }

