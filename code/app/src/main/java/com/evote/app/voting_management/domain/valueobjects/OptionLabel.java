package com.evote.app.voting_management.domain.valueobjects;

public class OptionLabel {

    private final String value;

    public OptionLabel(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Option darf nicht null sein");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Option darf nicht leer sein");
        }

        // Nur Buchstaben, Ziffern und Leerzeichen – kein "!"
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean letterOrDigit = Character.isLetterOrDigit(c);
            boolean space = c == ' ';
            if (!letterOrDigit && !space) {
                throw new IllegalArgumentException("Option enthält unzulässige Zeichen");
            }
        }

        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }
}
