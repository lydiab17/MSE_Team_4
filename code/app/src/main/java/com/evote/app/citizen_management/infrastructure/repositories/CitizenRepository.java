package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import java.util.Optional;

/**
 * Repository-Interface für den Zugriff auf Citizen-Daten.
 *
 * <p>Definiert grundlegende Persistenz-Operationen zum Speichern,
 * Löschen und Suchen von {@link Citizen}-Entitäten.</p>
 */
public interface CitizenRepository {

    /**
     * Speichert einen {@link Citizen}.
     *
     * @param citizen die zu speichernde Citizen-Entität
     */
    void save(Citizen citizen);

    /**
     * Entfernt alle gespeicherten {@link Citizen}-Einträge
     * aus dem Repository.
     */
    void clear();

    /**
     * Sucht einen {@link Citizen} anhand seiner eindeutigen
     * E-Mail-Adresse.
     *
     * @param email die eindeutige E-Mail-Adresse des Bürgers
     * @return ein {@link Optional}, das einen {@link Citizen} enthält,
     *         wenn ein entsprechender Eintrag gefunden wurde,
     *         oder {@link Optional#empty()}, wenn kein Eintrag existiert
     */
    Optional<Citizen> findByEmail(Email email);
}
