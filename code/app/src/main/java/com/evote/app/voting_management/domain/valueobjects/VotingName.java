package com.evote.app.voting_management.domain.valueobjects;

public class VotingName {

    private final String value;

    public VotingName(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Name darf nicht null sein");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }

        int length = trimmed.length();
        if (length < 10) {
            throw new IllegalArgumentException("Name muss mindestens 10 Zeichen haben");
        }
        if (length > 100) {
            throw new IllegalArgumentException("Name darf höchstens 100 Zeichen haben");
        }

        // Erster Buchstabe muss groß sein (inkl. Ä/Ö/Ü usw.)
        char first = trimmed.charAt(0);
        if (!Character.isUpperCase(first)) {
            throw new IllegalArgumentException("Name muss mit Großbuchstaben beginnen");
        }

        // Nur Buchstaben, Ziffern und Leerzeichen
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            boolean letterOrDigit = Character.isLetterOrDigit(c);
            boolean space = c == ' ';
            if (!letterOrDigit && !space) {
                throw new IllegalArgumentException("Name enthält unzulässige Zeichen");
            }
        }

        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }
}
