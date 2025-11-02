package io.github.lydiab17.mseteam4;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Voting {
    private int votingID;
    private String name;
    private String info;
    private boolean votingStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<String> options;

    /** Regex Pattern für Name, Info und Options
     * Name = Fängt mit Großbuchstaben an. Mindestens 10 Zeichen lang
     * Info = Fängt mit Großbuchstaben an. Mindestens 30 Zeichen lang
     * Optiinen = Fängt mit einem beliebigen Zeichen an und ist mindestens 1 - max 50 Zeichen lang*/
    private static final Pattern NAME_RE = Pattern.compile("^[A-ZÄÖÜ][A-Za-zÄÖÜäöüß0-9 ]{9,99}$");
    private static final Pattern INFO_RE = Pattern.compile("^[A-ZÄÖÜ].{29,999}$", Pattern.DOTALL);
    private static final Pattern OPT_RE  = Pattern.compile("^[A-Za-zÄÖÜäöüß0-9 ]{1,50}$");

    /** Beim initialisieren des records werden start und end Datum direkt zugewiesen. Können über
     * period.start() und period.end() aufgerufen werden.
     * Beim erstellen des Record wird geprüft ob Start vor End ist und ggfs. eine Exception geworfen.
     * contains Methode wird aktuell nicht verwendet. Kann im Zweifel später genutzt werden um zu prüfen ob ein Datum
     * innerhalb von Start und End liegt.*/
    private record Period(LocalDate start, LocalDate end) {
        Period {
            Objects.requireNonNull(start, "start");
            Objects.requireNonNull(end, "end");
            if (end.isBefore(start)) throw new IllegalArgumentException("Invalid dates");
        }
        boolean contains(LocalDate d) {
            return !d.isBefore(start) && !d.isAfter(end);
        }
    }

    private Voting() {}

    /** Methode um eine neue Abstimmung zu erstellen. */
    public static Voting create(int id, String name, String info, LocalDate start, LocalDate end, Set<String> options) {
        if (id <= 0) throw new IllegalArgumentException("Invalid id");

        // Optional für sauberes Trim + Null-Check in einem Schritt. Trim entfernt Leerzeichen am Anfang und Ende.
        String n = Optional.ofNullable(name).map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("name"));
        String i = Optional.ofNullable(info).map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("info"));

        Objects.requireNonNull(options, "options");
        // Regex-Checks für Name und Info
        if (!NAME_RE.matcher(n).matches()) throw new IllegalArgumentException("Invalid name");
        if (!INFO_RE.matcher(i).matches()) throw new IllegalArgumentException("Invalid info");

        // Period (Record) validiert Datumsordnung
        Period period = new Period(start, end);

        // Prüft ob die Anzahl der übergebenen Optionen ok ist
        if (options.size() < 2 || options.size() > 10) throw new IllegalArgumentException("Invalid options size");

        // Streams: trimmen, token prüfen, in definierter Reihenfolge einsammeln
        // peak schaut sich das aktuelle objekt an
        List<String> normalized = options.stream()
                .peek(o -> { if (o == null) throw new IllegalArgumentException("Null option"); })
                .map(String::trim)
                .peek(t -> { if (!OPT_RE.matcher(t).matches()) throw new IllegalArgumentException("Invalid option: " + t); })
                .collect(Collectors.toCollection(ArrayList::new));

        // Generics + Lambda: Duplikate auf einem beliebigen Schlüssel verbieten (hier: lower-case)
        // Optionen werden hier überprüft
        requireNoDuplicate(normalized, s -> s.toLowerCase(Locale.ROOT), dup ->
                new IllegalArgumentException("Duplicate option: " + dup));

        Voting v = new Voting();
        v.votingID = id;
        v.name = n;
        v.info = i;
        v.startDate = period.start();
        v.endDate = period.end();
        // Unmodifiable + Reihenfolge erhalten
        v.options = Collections.unmodifiableSet(new LinkedHashSet<>(normalized));
        v.votingStatus = false;
        return v;
    }

    // Kleines generisches Hilfs-Utility: verbietet Duplikate anhand eines Key-Extractors
    private static <T, K> void requireNoDuplicate(
            Iterable<T> items,
            Function<T, K> keyExtractor,
            Function<T, RuntimeException> onDuplicate) {

        // sets dürfen ohnehin keine doppelten werte haben
        Set<K> seen = new HashSet<>();
        for (T item : items) {
            K key = keyExtractor.apply(item);
            if (!seen.add(key)) throw onDuplicate.apply(item);
        }
    }

    public boolean isOpen(Clock clock) {
        if (!votingStatus) return false;
        LocalDate now = LocalDate.now(clock);
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    // Getter/Setter, nur was für die Tests nötig ist:
    public boolean isVotingStatus() { return votingStatus; }
    public void setVotingStatus(boolean status) { this.votingStatus = status; }
}
