package com.evote.app.votingmanagement.interfaces.dto;

/**
 * Request-DTO zum Abgeben einer Stimme.
 */
public record CastVoteRequest(
        String voterKey,
        String optionId
) {
}
