package com.evote.app.votingmanagement.domain.model;

import com.evote.app.votingmanagement.domain.valueobjects.OptionLabel;
import com.evote.app.votingmanagement.domain.valueobjects.VotingInfo;
import com.evote.app.votingmanagement.domain.valueobjects.VotingName;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

/**
 * Die Klasse repräsentiert die Entität Voting.
 *
 * @author Fabian
 * @version 1.0*/
public class Voting {

    private final int id;
    private final VotingName name;
    private final VotingInfo info;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<OptionLabel> options;

    private boolean votingStatus; // false: noch nicht freigeschaltet

    private Voting(int id,
                   VotingName name,
                   VotingInfo info,
                   LocalDate startDate,
                   LocalDate endDate,
                   List<OptionLabel> options) {
        this.id = id;
        this.name = name;
        this.info = info;
        this.startDate = startDate;
        this.endDate = endDate;
        this.options = options;
        this.votingStatus = false;
    }

    /** Erstellt ein Voting Object.
     * Regeln:
     *  * <ul>
     *  *   <li>10–100 Zeichen</li>
     *  *   <li>beginnt mit Großbuchstaben (inkl. Umlaute)</li>
     *  *   <li>nur Buchstaben, Ziffern und Leerzeichen</li>
     *  * Ungültige Werte führen zu IllegalArgumentException im Konstruktor.
     *  * </ul>*/
    public static Voting create(int id,
                                String rawName,
                                String rawInfo,
                                LocalDate startDate,
                                LocalDate endDate,
                                Set<String> rawOptions) {

        // null-Prüfungen
        Objects.requireNonNull(startDate, "startDate darf nicht null sein");
        Objects.requireNonNull(endDate, "endDate darf nicht null sein");
        Objects.requireNonNull(rawOptions, "options darf nicht null sein");

        // Anzahl Optionen prüfen
        int optionCount = rawOptions.size();
        if (optionCount < 2 || optionCount > 10) {
            throw new IllegalArgumentException("Anzahl Optionen muss zwischen 2 und 10 liegen");
        }

        // Datumsreihenfolge prüfen
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate darf nicht vor startDate liegen");
        }

        // Value Objects erledigen die restliche Validierung (null, Länge, Pattern, …)
        VotingName name = new VotingName(rawName);
        VotingInfo info = new VotingInfo(rawInfo);

        // Optionen: Inhalt prüfen (OptionLabel) + Duplikate (case-insensitive)
        List<OptionLabel> optionLabels = new ArrayList<>();
        Set<String> lowerCaseSet = new HashSet<>();

        // Die Schleife läuft so oft, wie es Optionen gibt
        for (String opt : rawOptions) {
            OptionLabel label = new OptionLabel(opt); // ValueObject OptionLabel prüft Regex einschränkungen
            String key = label.getValue().toLowerCase(Locale.ROOT);

            // Überprüfung, ob doppelte Optionen vorhanden sind
            if (!lowerCaseSet.add(key)) {
                throw new IllegalArgumentException("Optionen dürfen sich nur im Case unterscheiden");
            }

            optionLabels.add(label);
        }

        return new Voting(id, name, info, startDate, endDate, optionLabels);
    }

    // --- Status & isOpen-Logik ---

    public boolean isVotingStatus() {
        return votingStatus;
    }

    public void setVotingStatus(boolean votingStatus) {
        this.votingStatus = votingStatus;
    }

    public boolean isOpen(Clock clock) {
        if (!votingStatus) {
            return false;
        }
        LocalDate today = LocalDate.now(clock);
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    // --- ein paar einfache Getter ---

    public int getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public String getInfo() {
        return info.getValue();
    }

    public List<String> getOptionTexts() {
        List<String> list = new ArrayList<>();
        for (OptionLabel label : options) {
            list.add(label.getValue());
        }
        return Collections.unmodifiableList(list);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
