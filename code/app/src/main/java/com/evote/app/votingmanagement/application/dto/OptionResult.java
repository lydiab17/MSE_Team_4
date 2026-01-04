package com.evote.app.votingmanagement.application.dto;

/**
 * DTO für das Ergebnis einer Abstimmungsoption.
 *
 * @param option die Bezeichnung der Option
 * @param count  die Anzahl der Stimmen für diese Option
 */
public record OptionResult(
        String option,
        long count
) {
}
