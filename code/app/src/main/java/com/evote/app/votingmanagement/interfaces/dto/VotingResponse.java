package com.evote.app.votingmanagement.interfaces.dto;

import com.evote.app.votingmanagement.domain.model.Voting;
import java.time.LocalDate;

public record VotingResponse(
        int id,
        String name,
        String info,
        LocalDate startDate,
        LocalDate endDate,
        boolean open
) {
    public static VotingResponse fromDomain(Voting v) {
        return new VotingResponse(
                v.getId(),
                v.getName(),
                v.getInfo(),
                v.getStartDate(),
                v.getEndDate(),
                v.isVotingStatus()
        );
    }
}
