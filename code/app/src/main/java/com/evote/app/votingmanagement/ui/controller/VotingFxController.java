package com.evote.app.votingmanagement.ui.controller;

import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

/**
 * JavaFX-Controller für die Voting-Oberfläche.
 * Oben: Voting anlegen.
 * Unten links: nicht offene Votings.
 * Unten rechts: offene Votings.
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
    private ListView<Voting> openVotingsList;

    @FXML
    private ListView<Voting> notOpenVotingsList;

    @FXML
    private Label statusLabel;

    public VotingFxController(VotingApplicationService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        // hübsche Darstellung in beiden Listen
        openVotingsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Voting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatVoting(item));
            }
        });

        notOpenVotingsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Voting item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatVoting(item));
            }
        });

        refreshVotingLists();
    }

    // --------- Aktionen aus dem FXML ---------

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
            refreshVotingLists();
        } catch (Exception e) {
            showError("Fehler beim Anlegen", e.getMessage());
        }
    }

    @FXML
    private void onClearForm() {
        clearForm();
        statusLabel.setText("Formular geleert.");
    }

    @FXML
    private void onLoadSelectedVoting() {
        try {
            Voting selected = getSelectedVoting();
            if (selected == null) {
                throw new IllegalArgumentException("Bitte ein Voting in einer Liste auswählen.");
            }
            fillFormFromVoting(selected);
            statusLabel.setText("Voting " + selected.getId() + " geladen.");
        } catch (Exception e) {
            showError("Fehler beim Laden", e.getMessage());
        }
    }

    @FXML
    private void onOpenSelectedVoting() {
        try {
            Voting selected = notOpenVotingsList
                    .getSelectionModel()
                    .getSelectedItem();

            if (selected == null) {
                throw new IllegalArgumentException(
                        "Bitte ein nicht geöffnetes Voting in der linken Liste auswählen.");
            }

            service.openVoting(selected.getId());
            statusLabel.setText("Voting " + selected.getId() + " wurde geöffnet.");
            refreshVotingLists();
        } catch (Exception e) {
            showError("Fehler beim Öffnen", e.getMessage());
        }
    }

    @FXML
    private void onRefreshVotingLists() {
        try {
            refreshVotingLists();
            statusLabel.setText("Listen aktualisiert.");
        } catch (Exception e) {
            showError("Fehler beim Aktualisieren", e.getMessage());
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

    private void refreshVotingLists() {
        var open = service.getOpenVotings(Clock.systemDefaultZone());
        var notOpen = service.getNotOpenVotings();

        openVotingsList.setItems(FXCollections.observableArrayList(open));
        notOpenVotingsList.setItems(FXCollections.observableArrayList(notOpen));
    }

    private void clearForm() {
        // ID lasse ich drin, damit man gezielt neue IDs vergeben kann
        nameField.clear();
        infoArea.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        optionsField.clear();
    }

    private void fillFormFromVoting(Voting voting) {
        idField.setText(String.valueOf(voting.getId()));
        nameField.setText(voting.getName());
        infoArea.setText(voting.getInfo());
        startDatePicker.setValue(voting.getStartDate());
        endDatePicker.setValue(voting.getEndDate());
        optionsField.setText(
                voting.getOptionTexts().stream().collect(Collectors.joining(", "))
        );
    }

    /**
     * Nimmt zuerst Auswahl aus „nicht offene“, wenn dort nichts ausgewählt ist,
     * dann aus „offene“ Liste.
     */
    private Voting getSelectedVoting() {
        Voting selected = notOpenVotingsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            return selected;
        }
        return openVotingsList.getSelectionModel().getSelectedItem();
    }

    private String formatVoting(Voting v) {
        return String.format(
                "ID %d - %s [%s bis %s]%s",
                v.getId(),
                v.getName(),
                v.getStartDate(),
                v.getEndDate(),
                v.isVotingStatus() ? " (offen)" : ""
        );
    }

    private void showError(String title, String message) {
        statusLabel.setText("Fehler: " + message);

        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Fehler");
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
