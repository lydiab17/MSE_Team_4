package com.evote.app.votingmanagement.events;

import java.time.LocalDate;

/**
 * Domain-Event: Ein neues Voting wurde angelegt.
 */
public record VotingCreatedEvent(
        int id,
        String name,
        LocalDate startDate,
        LocalDate endDate
) {
}
