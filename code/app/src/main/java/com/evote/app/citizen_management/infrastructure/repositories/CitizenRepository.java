package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.CitizenID;

import java.util.Optional;

public interface CitizenRepository {

    /**
     * Speichert ein Citizen-Objekt.
     * @param citizen citizen
     */
    void save(Citizen citizen);

    /**
     * Sucht einen Citizen anhand seiner eindeutigen CitizenID
     * @param citizenId die eindeutige CitizenID
     * @return Wenn ein Bürger mit der angegebenen CitizenID gefunden wird, enthält das Optional ein Citizen-Objekt. Wenn kein Bürger gefunden wird, ist das Optional leer.
     */
    Optional<Citizen> findById(CitizenID citizenId);
}
