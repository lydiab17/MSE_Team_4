package io.github.lydiab17.mseteam4;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Voting_R {
    private int votingID;
    private String name;
    private String info;
    private boolean votingStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<String> options;

    private static final int MIN_NAME_LEN = 10;
    private static final int MAX_NAME_LEN = 100;
    private static final int MIN_INFO_LEN = 30;
    private static final int MAX_INFO_LEN = 1000;
    private static final int MAX_OPTION_LEN = 50;

    /** Regex Pattern für Name, Info und Options
     * Name = Fängt mit Großbuchstaben an. Mindestens 10 Zeichen lang
     * Info = Fängt mit Großbuchstaben an. Mindestens 30 Zeichen lang
     * Optiinen = Fängt mit einem beliebigen Zeichen an und ist mindestens 1 - max 50 Zeichen lang*/
    private static final Pattern NAME_RE =
            Pattern.compile("^[A-ZÄÖÜ][A-Za-zÄÖÜäöüß0-9 ]{" + (MIN_NAME_LEN - 1) + "," + (MAX_NAME_LEN - 1) + "}$");

    private static final Pattern INFO_RE =
            Pattern.compile("^[A-ZÄÖÜ].{" + (MIN_INFO_LEN - 1) + "," + (MAX_INFO_LEN - 1) + "}$", Pattern.DOTALL);

    private static final Pattern OPT_RE =
            Pattern.compile("^[A-Za-zÄÖÜäöüß0-9 ]{1," + MAX_OPTION_LEN + "}$");


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

    /** Methode um eine neue Abstimmung zu erstellen. */
    public static Voting_R create(int id, String name, String info,
                                LocalDate start, LocalDate end,
                                Set<String> options) {

        if (id <= 0) throw new IllegalArgumentException("Invalid id");

        // Schrittweise Validierung
        String n = validateName(name);
        String i = validateInfo(info);
        Period period = validateDates(start, end);
        Set<String> opts = validateOptions(options);

        // Objekt aufbauen
        Voting_R v = new Voting_R();
        v.votingID = id;
        v.name = n;
        v.info = i;
        v.startDate = period.start();
        v.endDate = period.end();
        v.options = opts;
        v.votingStatus = false;
        return v;
    }

    private static String validateName(String name) {
        if (name == null) throw new IllegalArgumentException("Name must not be null");
        String trimmed = name.trim();
        if (!NAME_RE.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid name: must start uppercase and have 10–100 valid characters.");
        }
        return trimmed;
    }

    private static String validateInfo(String info) {
        if (info == null) throw new IllegalArgumentException("Info must not be null");
        String trimmed = info.trim();
        if (!INFO_RE.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid info: must start uppercase and have 30–1000 characters.");
        }
        return trimmed;
    }

    private static Period validateDates(LocalDate start, LocalDate end) {
        Objects.requireNonNull(start, "startDate must not be null");
        Objects.requireNonNull(end, "endDate must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("endDate must not be before startDate.");
        }
        return new Period(start, end);
    }

    private static Set<String> validateOptions(Set<String> options) {
        if (options == null) throw new NullPointerException("options must not be null");
        if (options.size() < 2 || options.size() > 10) {
            throw new IllegalArgumentException("Number of options must be between 2 and 10.");
        }

        List<String> normalized = new ArrayList<>();
        for (String o : options) {
            if (o == null) throw new IllegalArgumentException("Option must not be null");
            String t = o.trim();
            if (!OPT_RE.matcher(t).matches())
                throw new IllegalArgumentException("Invalid option text: " + t);
            normalized.add(t);
        }

        requireNoDuplicate(normalized, s -> s.toLowerCase(Locale.ROOT),
                dup -> new IllegalArgumentException("Duplicate option: " + dup));

        return Set.copyOf(new LinkedHashSet<>(normalized));
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
