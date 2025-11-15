package com.evote.app.voting_management.domain.valueobjects;

public class OptionLabel {

    private static final String PATTERN = "^[\\p{L}\\p{Nd} ]+$";

    private final String value;

    public OptionLabel(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Option darf nicht null sein");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Option darf nicht leer sein");
        }

        if (!trimmed.matches(PATTERN)) {
            throw new IllegalArgumentException("Option darf nur Buchstaben, Ziffern und Leerzeichen enthalten");
        }

        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }
}
