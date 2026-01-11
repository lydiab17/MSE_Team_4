package com.evote.app.citizen_management.infrastructure.repositories;

import com.evote.app.citizen_management.domain.model.Citizen;
import com.evote.app.citizen_management.domain.valueobjects.Email;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * In-Memory-Implementierung des {@link CitizenRepository}.
 *
 * <p>Diese Implementierung speichert {@link Citizen}-Objekte
 * in einer einfachen {@link Map} im Arbeitsspeicher.
 */
@Repository
public class InMemoryCitizenRepository implements CitizenRepository {

    /**
     * Interner Speicher für Citizen-Entitäten.
     *
     * <p>Der Schlüssel ist die eindeutige {@link Email},
     * der Wert das zugehörige {@link Citizen}-Objekt.</p>
     */
    private final Map<Email, Citizen> store = new HashMap<>();

    /**
     * Speichert oder überschreibt einen {@link Citizen} im In-Memory-Speicher.
     *
     * @param citizen die zu speichernde Citizen-Entität
     */
    @Override
    public void save(Citizen citizen) {
        store.put(citizen.getEmail(), citizen);
    }

    /**
     * Entfernt alle gespeicherten {@link Citizen}-Einträge
     * aus dem In-Memory-Speicher.
     */
    @Override
    public void clear() {
        store.clear();
    }

    /**
     * Sucht einen {@link Citizen} anhand seiner E-Mail-Adresse
     * im In-Memory-Speicher.
     *
     * @param email die eindeutige E-Mail-Adresse des Bürgers
     * @return ein {@link Optional} mit dem gefundenen {@link Citizen}
     *         oder {@link Optional#empty()}, falls kein Eintrag existiert
     */
    @Override
    public Optional<Citizen> findByEmail(Email email) {
        return Optional.ofNullable(store.get(email));
    }
}
