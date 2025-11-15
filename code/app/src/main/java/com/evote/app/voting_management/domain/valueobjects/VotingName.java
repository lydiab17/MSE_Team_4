package com.evote.app.voting_management.domain.valueobjects;

/**
 * Repräsentiert den Namen/Titel einer Abstimmung.
 *
 * Fachliche Regeln:
 * <ul>
 *     <li>darf nicht {@code null} sein</li>
 *     <li>darf nach dem Trimmen nicht leer sein</li>
 *     <li>Länge (nach Trim): mindestens 10, höchstens 100 Zeichen</li>
 *     <li>muss mit einem Großbuchstaben beginnen (inkl. Ä/Ö/Ü usw.)</li>
 *     <li>erlaubte Zeichen: Buchstaben, Ziffern und Leerzeichen</li>
 *     <li>keine Sonderzeichen wie {@code ! ? , .} usw.</li>
 * </ul>
 *
 * Ungültige Werte führen im Konstruktor zu einer {@link IllegalArgumentException}.
 */
public class VotingName {

    private final String value;

    private static final int MIN_LENGTH = 10;
    private static final int MAX_LENGTH = 100;
    // \\p{Lu} -> ein Großbuchstabe (Unicode Uppercase Letter, z.B. A–Z, Ä, Ö, Ü)
    // \\p{L} -> irgendein Buchstabe (Letter, auch Umlaute, andere Sprachen etc.)
    // \\p{Nd}-> Dezimalziffer (Number, decimal digit, 0–9)
    private static final String PATTERN = "^[\\p{Lu}][\\p{L}\\p{Nd} ]*$";

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
        int len = trimmed.length();
        if (len < MIN_LENGTH || len > MAX_LENGTH) {
            throw new IllegalArgumentException("Name muss zwischen 10 und 100 Zeichen lang sein");
        }

        if (!trimmed.matches(PATTERN)) {
            throw new IllegalArgumentException(
                    "Name muss mit Großbuchstaben beginnen und darf nur Buchstaben, Ziffern und Leerzeichen enthalten"
            );
        }

        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }
}
