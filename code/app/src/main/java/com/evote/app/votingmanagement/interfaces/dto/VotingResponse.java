package com.evote.app.votingmanagement.interfaces.dto;

import com.evote.app.votingmanagement.domain.model.Voting;
import java.time.LocalDate;
import java.util.List;

/**
 * Response-DTO zur Darstellung einer Abstimmung.
 *
 * @param id        ID der Abstimmung
 * @param name      Name/Titel der Abstimmung
 * @param info      zusätzliche Informationen/Beschreibung
 * @param startDate Startdatum
 * @param endDate   Enddatum
 * @param open      ob die Abstimmung aktuell geöffnet ist
 * @param options   auswählbare Optionen
 */
public record VotingResponse(
        int id,
        String name,
        String info,
        LocalDate startDate,
        LocalDate endDate,
        boolean open,
        List<String> options
) {

  /**
   * Konvertiert ein Domain-Objekt ({@link Voting}) in ein {@link VotingResponse}.
   *
   * @param v Voting aus der Domain
   * @return Response-DTO
   */
  public static VotingResponse fromDomain(Voting v) {
    return new VotingResponse(
            v.getId(),
            v.getName(),
            v.getInfo(),
            v.getStartDate(),
            v.getEndDate(),
            v.isVotingStatus(),
            v.getOptionTexts()
    );
  }
}
