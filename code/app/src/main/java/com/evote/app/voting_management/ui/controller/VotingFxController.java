package com.evote.app.voting_management.ui.controller;

import com.evote.app.voting_management.application.VotingApplicationService;
import com.evote.app.voting_management.domain.model.Voting;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JavaFX-Controller für die Voting-Oberfläche.
 * Bindet den VotingApplicationService ein und ermöglicht:
 * - Voting anlegen
 * - Voting öffnen
 * - Voting per ID laden
 * - offene Votings anzeigen
 */
@Component
public class VotingFxController {

    private final VotingApplicationService service;

    @FXML
    private TextField idField;

    @FXML
    private TextField nameField;

    @FXML
    private TextArea infoArea;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField optionsField;

    @FXML
    private ListView<String> openVotingsList;

    @FXML
    private Label statusLabel;

    public VotingFxController(VotingApplicationService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        // Beim Start direkt offene Votings laden
        refreshOpenVotings();
    }

    @FXML
    private void onCreateVoting() {
        try {
            int id = parseId();
            String name = nameField.getText();
            String info = infoArea.getText();
            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();
            Set<String> options = parseOptions(optionsField.getText());

            Voting voting = service.createVoting(id, name, info, start, end, options);

            statusLabel.setText("Voting " + voting.getId() + " erfolgreich angelegt.");
            clearForm();
            refreshOpenVotings();
        } catch (Exception e) {
            showError("Fehler beim Anlegen", e.getMessage());
        }
    }

    @FXML
    private void onOpenVoting() {
        try {
            int id = parseId();
            service.openVoting(id);
            statusLabel.setText("Voting " + id + " wurde geöffnet.");
            refreshOpenVotings();
        } catch (Exception e) {
            showError("Fehler beim Öffnen", e.getMessage());
        }
    }

    @FXML
    private void onLoadVoting() {
        try {
            int id = parseId();
            Voting voting = service.getVotingById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Voting mit ID " + id + " nicht gefunden"));

            // Felder mit den Daten füllen
            nameField.setText(voting.getName());
            infoArea.setText(voting.getInfo());
            startDatePicker.setValue(voting.getStartDate());
            endDatePicker.setValue(voting.getEndDate());
            optionsField.setText(
                    voting.getOptionTexts().stream().collect(Collectors.joining(", "))
            );

            statusLabel.setText("Voting " + id + " geladen.");
        } catch (Exception e) {
            showError("Fehler beim Laden", e.getMessage());
        }
    }

    @FXML
    private void onRefreshOpenVotings() {
        try {
            refreshOpenVotings();
            statusLabel.setText("Offene Votings aktualisiert.");
        } catch (Exception e) {
            showError("Fehler beim Laden der offenen Votings", e.getMessage());
        }
    }

    // ---------- Hilfsmethoden ----------

    private int parseId() {
        String text = idField.getText();
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("ID darf nicht leer sein");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("ID muss eine Zahl sein");
        }
    }

    private Set<String> parseOptions(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Optionen dürfen nicht leer sein");
        }
        Set<String> set = new LinkedHashSet<>();
        for (String part : raw.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                set.add(trimmed);
            }
        }
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Mindestens eine Option muss angegeben werden");
        }
        return set;
    }

    private void refreshOpenVotings() {
        var open = service.getOpenVotings(Clock.systemDefaultZone());
        var items = open.stream()
                .map(v -> "ID " + v.getId() + " - " + v.getName()
                        + " [" + v.getStartDate() + " bis " + v.getEndDate() + "]")
                .toList();

        openVotingsList.setItems(FXCollections.observableArrayList(items));
    }

    private void clearForm() {
        // ID lasse ich drin, damit man das gleiche Voting ggf. öffnen kann
        nameField.clear();
        infoArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        optionsField.clear();
    }

    private void showError(String title, String message) {
        statusLabel.setText("Fehler: " + message);

        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Fehler");
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
