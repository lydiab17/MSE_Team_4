package com.evote.app.votingmanagement.application.dto;

/**
 * DTO für CastVote.
 *
 * @param authToken Authentifizierungs-Token (z. B. JWT)
 * @param votingId ID der Abstimmung
 * @param optionId gewählte Option
 */
public record CastVoteDto(String authToken, int votingId, String optionId) {}
