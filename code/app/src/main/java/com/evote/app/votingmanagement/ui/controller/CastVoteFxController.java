package com.evote.app.votingmanagement.ui.controller;

import com.evote.app.votingmanagement.application.dto.CastVoteDto;
import com.evote.app.votingmanagement.application.services.VotingApplicationService;
import com.evote.app.votingmanagement.domain.model.Voting;
import java.time.Clock;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

/**
 * JavaFX-Controller für die Abstimm-Ansicht.
 * Links: offene Votings, rechts: Details & Optionen.
 */
@Component
public class CastVoteFxController {

    private final VotingApplicationService service;

    @FXML
    private ListView<Voting> openVotingsList;

    @FXML
    private ListView<String> optionsList;

    @FXML
    private Label selectedVotingTitle;

    @FXML
    private Label selectedVotingDates;

    @FXML
    private TextArea selectedVotingInfo;

    @FXML
    private TextField voterKeyField;

    @FXML
    private Label statusLabel;

    public CastVoteFxController(VotingApplicationService service) {
        this.service = service;
    }

    @FXML
    private void initialize() {
        // Schöne Darstellung der Votings
        openVotingsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Voting item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format(
                            "ID %d - %s [%s bis %s]",
                            item.getId(),
                            item.getName(),
                            item.getStartDate(),
                            item.getEndDate()
                    ));
                }
            }
        });

        // Wenn Voting ausgewählt wird, Details anzeigen
        openVotingsList.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldV, newV) -> showVotingDetails(newV));

        refreshOpenVotings();
    }

    // ---------- Aktionen aus dem FXML ----------

    @FXML
    private void onRefreshOpenVotings() {
        try {
            refreshOpenVotings();
            statusLabel.setText("Offene Abstimmungen aktualisiert.");
        } catch (Exception e) {
            showError("Fehler beim Laden der offenen Abstimmungen", e.getMessage());
        }
    }

    @FXML
    private void onCastVote() {
        try {
            Voting selected = openVotingsList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                throw new IllegalArgumentException("Bitte zuerst eine Abstimmung auswählen.");
            }

            String option = optionsList.getSelectionModel().getSelectedItem();
            if (option == null) {
                throw new IllegalArgumentException("Bitte eine Option auswählen.");
            }

            String voterKey = voterKeyField.getText();
            if (voterKey == null || voterKey.isBlank()) {
                throw new IllegalArgumentException("Bitte einen Voter-Key eingeben.");
            }

            CastVoteDto dto = new CastVoteDto(
                    voterKey,
                    selected.getId(),
                    option
            );

            service.castVote(dto);

            statusLabel.setText("Stimme für \"" + option + "\" erfolgreich abgegeben.");
            // Auswahl beibehalten, nur Key löschen
            voterKeyField.clear();

        } catch (Exception e) {
            showError("Fehler beim Abstimmen", e.getMessage());
        }
    }

    // ---------- Hilfsmethoden ----------

    private void refreshOpenVotings() {
        List<Voting> open = service.getOpenVotings(Clock.systemDefaultZone());
        openVotingsList.setItems(FXCollections.observableArrayList(open));

        // Wenn es eine Auswahl gab, Details aktualisieren,
        // sonst Details leeren
        Voting selected = openVotingsList.getSelectionModel().getSelectedItem();
        showVotingDetails(selected);
    }

    private void showVotingDetails(Voting voting) {
        if (voting == null) {
            selectedVotingTitle.setText("");
            selectedVotingDates.setText("");
            selectedVotingInfo.clear();
            optionsList.setItems(FXCollections.emptyObservableList());
            return;
        }

        selectedVotingTitle.setText(
                String.format("ID %d – %s", voting.getId(), voting.getName())
        );
        selectedVotingDates.setText(
                String.format("Zeitraum: %s bis %s", voting.getStartDate(), voting.getEndDate())
        );
        selectedVotingInfo.setText(voting.getInfo());

        optionsList.setItems(
                FXCollections.observableArrayList(voting.getOptionTexts())
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
