package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.CitizenID;

import java.util.Optional;

public interface CitizenRepository {

    void save(Citizen citizen);
    Optional<Citizen> findById(CitizenID citizenId);
    void delete(CitizenID citizenId);
}
