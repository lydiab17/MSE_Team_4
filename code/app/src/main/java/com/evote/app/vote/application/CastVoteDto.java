package com.evote.app.vote.application;

/**
 * Einfaches DTO für CastVote.
 */
public final class CastVoteDto {
    public final String authToken;
    public final String votingId;
    public final String optionId;

    public CastVoteDto(String authToken, String votingId, String optionId) {
        this.authToken = authToken;
        this.votingId = votingId;
        this.optionId = optionId;
    }
}
