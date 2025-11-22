package com.evote.app.vote.domain;

import java.util.Objects;
import java.util.UUID;

public class VotingId {
    private final String value;

    private VotingId(String value) {
        this.value = Objects.requireNonNull(value);
    }

    public static VotingId of(String value) {
        return new VotingId(value);
    }

    public static VotingId random() {
        return new VotingId(UUID.randomUUID().toString());
    }

    public String value() { return value; }

    @Override
    public String toString() { return value; }
}
