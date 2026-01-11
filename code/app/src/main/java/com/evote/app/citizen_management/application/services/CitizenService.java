package com.evote.app.citizen_management.application.services;

import com.evote.app.citizen_management.application.CitizenAggregator;
import com.evote.app.citizen_management.application.commands.CitizenRegistrationCommand;
import com.evote.app.citizen_management.application.dto.CitizenDto;
import com.evote.app.citizen_management.application.dto.CitizenRegistrationRequestDto;
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

/**
 * Application Service zur Orchestrierung der Citizen-Use-Cases.
 *
 * <p>Der {@code CitizenService} implementiert die fachlichen Anwendungsfälle
 * rund um Bürger (z. B. Registrierung, Login, Abfrage des aktuell eingeloggten
 * Bürgers). Er koordiniert dabei die Zusammenarbeit zwischen Aggregator,
 * Projector und Repositories.</p>
 *
 * <p>Der Service enthält selbst keine fachlichen Entscheidungen, sondern
 * steuert den Ablauf der Use-Cases und delegiert Entscheidungen an den
 * {@link CitizenAggregator} sowie die technische Umsetzung an den
 * {@link CitizenProjector}.</p>
 */
@Service
public class CitizenService {

    /**
     * Repository zum Lesen von Citizen-Daten (Read Repository).
     */
    private final CitizenRepository citizenRepository;

    /**
     * Event Store zur Speicherung und Verwaltung von Domain Events.
     */
    private final EventStore eventRepository;

    /**
     * Projector zur Umsetzung von Domain Events in persistierbaren Zustand.
     */
    private final CitizenProjector citizenProjector;

    /**
     * Aggregator zur fachlichen Verarbeitung von Citizen-Commands.
     */
    private final CitizenAggregator citizenAggregator;

    /**
     * Erstellt einen neuen {@code CitizenService}.
     *
     * @param citizenRepository Repository zum Lesen von Citizen-Daten
     * @param citizenAggregator Aggregator zur fachlichen Command-Verarbeitung
     * @param citizenProjector Projector zur Umsetzung von Domain Events
     * @param eventStore Event Store zur Speicherung von Domain Events
     */
    public CitizenService(
            CitizenRepository citizenRepository,
            CitizenAggregator citizenAggregator,
            CitizenProjector citizenProjector,
            EventStore eventStore) {
        this.citizenRepository = citizenRepository;
        this.eventRepository = eventStore;
        this.citizenProjector = citizenProjector;
        this.citizenAggregator = citizenAggregator;
    }

    /**
     * Use Case: Registrierung eines neuen Bürgers.
     *
     * <p>Die vom Client übergebenen Registrierungsdaten werden zunächst in einen
     * {@link CitizenRegistrationCommand} umgewandelt. Dieser Command wird an den
     * {@link CitizenAggregator} delegiert, der die fachliche Entscheidung trifft
     * und entsprechende Domain Events erzeugt.</p>
     *
     * <p>Das erzeugte {@link CitizenCreatedEvent} wird anschließend durch den
     * {@link CitizenProjector} in einen konkreten Citizen-Zustand projiziert
     * und persistiert.</p>
     *
     * @param registrationInput DTO mit den Registrierungsdaten des Bürgers
     * @return der neu registrierte {@link Citizen}
     * @throws UserAlreadyExistsException wenn bereits ein Bürger mit der angegebenen E-Mail existiert
     */
    public Citizen registerCitizen(CitizenRegistrationRequestDto registrationInput)
            throws UserAlreadyExistsException {
        // DTO in Command übersetzen
        CitizenRegistrationCommand citizenRegistrationCommand =
                new CitizenRegistrationCommand(
                        registrationInput.firstName(),
                        registrationInput.lastName(),
                        registrationInput.email(),
                        registrationInput.password());
        // Command an den Aggregator geben
        DomainEvent event = this.citizenAggregator.handle(citizenRegistrationCommand).get(0);
        // Event an Projector übergeben
        return this.citizenProjector.apply((CitizenCreatedEvent) event);
    }

    /**
     * Use Case: Login eines Bürgers.
     *
     * <p>Überprüft, ob ein Bürger mit der angegebenen E-Mail existiert und ob
     * das übergebene Passwort mit dem gespeicherten Passwort übereinstimmt.</p>
     *
     * @param email E-Mail-Adresse des Bürgers
     * @param passwort Passwort des Bürgers
     * @return {@code true} bei erfolgreicher Authentifizierung, sonst {@code false}
     */
    public boolean loginCitizen(String email, String passwort) {
        // String -> ValueObject
        Email emailObj = new Email(email);

        // Zugriff auf Read-Repository
        // falls Citizen existiert: Passwort aus Domain-Objekt holen und mit übergebenem Passwort vergleichen
        return citizenRepository
                .findByEmail(emailObj)
                .map(c -> c.getPassword().password().equals(passwort))
                .orElse(false);
    }

    /**
     * Liefert die Daten des aktuell eingeloggten Bürgers.
     *
     * <p>Die E-Mail-Adresse des aktuell authentifizierten Benutzers wird aus dem
     * Spring Security Context gelesen. Anschließend werden die zugehörigen
     * Bürgerdaten aus dem {@link CitizenRepository} geladen und als
     * {@link CitizenDto} zurückgegeben.</p>
     *
     * @return DTO mit den Daten des aktuell eingeloggten Bürgers
     */
    public CitizenDto getCurrentLoggedInCitizen() {
        String mail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Citizen citizen = this.citizenRepository.findByEmail(new Email(mail)).orElseThrow();
        return new CitizenDto(
                citizen.getCitizenID().toString(),
                citizen.getName().firstName(),
                citizen.getName().lastName(),
                citizen.getEmail().email(),
                citizen.getPassword().password());
    }
}
