package com.evote.app.votingmanagement.interfaces.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Request-DTO zum Erstellen einer Abstimmung.
 *
 * @param id        eindeutige ID der Abstimmung
 * @param name      Name/Titel der Abstimmung
 * @param info      zusätzliche Informationen/Beschreibung
 * @param startDate Startdatum der Abstimmung
 * @param endDate   Enddatum der Abstimmung
 * @param options   Liste der auswählbaren Optionen
 */
public record CreateVotingRequest(
        int id,
        String name,
        String info,
        LocalDate startDate,
        LocalDate endDate,
        List<String> options
) {
}
