package com.evote.app.votingmanagement.interfaces.dto;

import com.evote.app.votingmanagement.application.dto.OptionResult;

/**
 * Response-DTO für ein Ergebnis einer Abstimmungsoption.
 *
 * @param option die Bezeichnung der Option
 * @param count  die Anzahl der Stimmen für diese Option
 */
public record OptionResultResponse(
        String option,
        long count
) {

  /**
   * Konvertiert ein Application-DTO ({@link OptionResult}) in ein Response-DTO.
   *
   * @param r das Ergebnis aus der Anwendungsschicht
   * @return Response-DTO
   */
  public static OptionResultResponse fromOptionResult(OptionResult r) {
    return new OptionResultResponse(r.option(), r.count());
  }
}
