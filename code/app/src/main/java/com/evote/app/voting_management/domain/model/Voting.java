package com.evote.app.voting_management.domain.model;

import com.evote.app.voting_management.domain.valueobjects.OptionLabel;
import com.evote.app.voting_management.domain.valueobjects.VotingInfo;
import com.evote.app.voting_management.domain.valueobjects.VotingName;

import java.time.Clock;
import java.time.LocalDate;
import java.util.*;

public class Voting {

    private final int id;
    private final VotingName name;
    private final VotingInfo info;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<OptionLabel> options;

    private boolean votingStatus; // false beim Start

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
        this.votingStatus = false; // explizit
    }

    // Fabrikmethode, die dein Test benutzt
    public static Voting create(int id,
                                String rawName,
                                String rawInfo,
                                LocalDate startDate,
                                LocalDate endDate,
                                Set<String> rawOptions) {

        // Name/Info: laut Test IllegalArgumentException bei null
        if (rawName == null) {
            throw new IllegalArgumentException("Name darf nicht null sein");
        }
        if (rawInfo == null) {
            throw new IllegalArgumentException("Info darf nicht null sein");
        }

        // Dates / Options: laut Test NullPointerException bei null
        Objects.requireNonNull(startDate, "startDate darf nicht null sein");
        Objects.requireNonNull(endDate, "endDate darf nicht null sein");
        Objects.requireNonNull(rawOptions, "options darf nicht null sein");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate darf nicht vor startDate liegen");
        }

        if (rawOptions.size() < 2 || rawOptions.size() > 10) {
            throw new IllegalArgumentException("Anzahl Optionen muss zwischen 2 und 10 liegen");
        }

        VotingName name = new VotingName(rawName);
        VotingInfo info = new VotingInfo(rawInfo);

        // Optionen prüfen: Duplikate (case-insensitive) und Inhalt
        List<OptionLabel> optionLabels = new ArrayList<>();
        Set<String> lowerCaseSet = new HashSet<>();

        for (String opt : rawOptions) {
            OptionLabel label = new OptionLabel(opt);
            String key = label.getValue().toLowerCase(Locale.ROOT);

            if (lowerCaseSet.contains(key)) {
                throw new IllegalArgumentException("Optionen dürfen sich nicht nur im Case unterscheiden");
            }
            lowerCaseSet.add(key);
            optionLabels.add(label);
        }

        return new Voting(id, name, info, startDate, endDate, optionLabels);
    }

    // Getter für Status (wird im Test verwendet)
    public boolean isVotingStatus() {
        return votingStatus;
    }

    public void setVotingStatus(boolean votingStatus) {
        this.votingStatus = votingStatus;
    }

    // isOpen-Logik: nur offen, wenn Status=true UND Datum im Intervall [start, end]
    public boolean isOpen(Clock clock) {
        if (!votingStatus) {
            return false;
        }
        LocalDate today = LocalDate.now(clock);
        boolean notBeforeStart = !today.isBefore(startDate);
        boolean notAfterEnd = !today.isAfter(endDate);
        return notBeforeStart && notAfterEnd;
    }

    // Falls du später mehr brauchst, kannst du weitere Getter hinzufügen
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
        List<String> result = new ArrayList<>();
        for (OptionLabel label : options) {
            result.add(label.getValue());
        }
        return Collections.unmodifiableList(result);
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
