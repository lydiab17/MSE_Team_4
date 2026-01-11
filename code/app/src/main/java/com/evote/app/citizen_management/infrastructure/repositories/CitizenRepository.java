package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;

import java.util.Optional;
/**
 * Repository-Schnittstelle.
 * Definiert grundlegende Operationen zum Speichern, Löschen
 * und Suchen von Citizen-Objekten.
 *
 * @author Lydia Boes
 * @version 2.0
 */
public interface CitizenRepository {

    /**
     * Speichert ein Citizen-Objekt.
     *
     * @param citizen der zu speichernde Bürger
     */
    void save(Citizen citizen);

    /**
     * Entfernt alle gespeicherten Citizen-Objekte aus dem Repository.
     */
    void clear();

    /**
     * Sucht einen Citizen anhand seiner eindeutigen E-Mail
     * @param email die eindeutige E-Mail
     * @return Wenn ein Bürger mit der angegebenen E-Mail gefunden wird, enthält das Optional ein Citizen-Objekt. Wenn kein Bürger gefunden wird, ist das Optional leer.
     */
    Optional<Citizen> findByEmail(Email email);
}
