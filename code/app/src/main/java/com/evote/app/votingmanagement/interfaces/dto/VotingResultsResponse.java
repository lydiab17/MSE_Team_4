package com.evote.app.votingmanagement.interfaces.dto;

import java.util.List;

public record VotingResultsResponse(
        int votingId,
        List<OptionResultResponse> results
) {
}
