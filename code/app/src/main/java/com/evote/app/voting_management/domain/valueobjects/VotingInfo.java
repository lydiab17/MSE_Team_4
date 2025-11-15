package com.evote.app.voting_management.domain.valueobjects;

public class VotingInfo {

    private final String value;

    public VotingInfo(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Info darf nicht null sein");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Info darf nicht leer sein");
        }

        int length = trimmed.length();
        if (length < 30) {
            throw new IllegalArgumentException("Info muss mindestens 30 Zeichen haben");
        }
        if (length > 1000) {
            throw new IllegalArgumentException("Info darf höchstens 1000 Zeichen haben");
        }

        // Erster Buchstabe muss groß sein
        char first = trimmed.charAt(0);
        if (!Character.isUpperCase(first)) {
            throw new IllegalArgumentException("Info muss mit Großbuchstaben beginnen");
        }

        // Hier erlauben wir alle weiteren Zeichen (inkl. Punkt, Zeilenumbruch, …)
        this.value = raw;
    }

    public String getValue() {
        return value;
    }
}
