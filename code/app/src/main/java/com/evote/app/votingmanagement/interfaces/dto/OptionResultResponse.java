package com.evote.app.votingmanagement.interfaces.dto;


import com.evote.app.votingmanagement.application.dto.OptionResult;

public record OptionResultResponse(
        String option,
        long count
) {
    public static OptionResultResponse fromOptionResult(OptionResult r) {
        return new OptionResultResponse(r.option(), r.count());
    }
}
