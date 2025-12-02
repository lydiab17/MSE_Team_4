package com.evote.app.votingmanagement.interfaces.dto;

import java.time.LocalDate;
import java.util.List;

public record CreateVotingRequest(
        int id,
        String name,
        String info,
        LocalDate startDate,
        LocalDate endDate,
        List<String> options
) {
}
