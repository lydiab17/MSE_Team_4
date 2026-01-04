package com.evote.app.votingmanagement.interfaces.dto;

import java.util.List;

/**
 * Response-DTO für die Ergebnisse einer Abstimmung.
 *
 * @param votingId ID der Abstimmung
 * @param results  Liste der Ergebnisse je Option
 */
public record VotingResultsResponse(
        int votingId,
        List<OptionResultResponse> results
) {
}
