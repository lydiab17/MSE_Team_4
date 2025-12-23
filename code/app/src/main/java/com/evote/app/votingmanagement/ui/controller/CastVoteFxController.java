package com.evote.app.votingmanagement.ui.controller;

import com.evote.app.citizen_management.ui.controller.MainController;
import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.ui.api.VotingApiClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * JavaFX-Controller für vote-view.fxml – kommuniziert per REST mit dem Backend.
 */
@Component
public class CastVoteFxController {

    private final VotingApiClient apiClient;
    private final AuthSession authSession;

    @FXML private ListView<VotingResponse> openVotingsList;
    @FXML private ListView<String> optionsList;

    @FXML private Label selectedVotingTitle;
    @FXML private Label selectedVotingDates;
    @FXML private TextArea selectedVotingInfo;

    // ist in deiner FXML noch drin – wird aber mit JWT nicht mehr gebraucht
    @FXML private TextField voterKeyField;

    @FXML private Label statusLabel;

    private VotingResponse selectedVoting;

    public CastVoteFxController(VotingApiClient apiClient, AuthSession authSession) {
        this.apiClient = apiClient;
        this.authSession = authSession;
    }

    @FXML
    private void initialize() {
        // Optional: voterKeyField verstecken/disable, wenn JWT vorhanden
        if (voterKeyField != null) {
            voterKeyField.setDisable(true);
            voterKeyField.setManaged(false);
            voterKeyField.setVisible(false);
        }

        // ListView schön rendern (ID + Name)
        openVotingsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(VotingResponse item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("ID " + item.id() + " – " + item.name());
                }
            }
        });

        // Wenn Voting ausgewählt: Details laden + Optionen anzeigen
        openVotingsList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                loadVotingDetails(newV.id());
            }
        });

        optionsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        onRefreshOpenVotings();
    }

    @FXML
    private void onRefreshOpenVotings() {
        runAsync(
                () -> apiClient.getOpenVotings(),
                votings -> {
                    openVotingsList.setItems(FXCollections.observableArrayList(votings));
                    statusLabel.setText("Offene Abstimmungen geladen: " + votings.size());
                }
        );
    }

    @FXML
    private void onGoToVotingView() {
        MainController.getInstance().changeView("voting-view");
    }


    private void loadVotingDetails(int votingId) {
        runAsync(
                () -> apiClient.getById(votingId),
                voting -> {
                    this.selectedVoting = voting;

                    selectedVotingTitle.setText(voting.name());
                    selectedVotingDates.setText(voting.startDate() + " bis " + voting.endDate());
                    selectedVotingInfo.setText(voting.info());

                    // WICHTIG: VotingResponse muss options enthalten, sonst geht das nicht
                    // -> wenn deine VotingResponse keine options() hat, sag kurz Bescheid.
                    List<String> opts = voting.options();
                    optionsList.setItems(FXCollections.observableArrayList(opts));

                    statusLabel.setText("Voting geladen: ID " + voting.id());
                }
        );
    }

    @FXML
    private void onCastVote() {
        if (authSession.token().isEmpty()) {
            showAlert(AlertType.ERROR, "Nicht eingeloggt", "Bitte zuerst einloggen.");
            return;
        }

        if (selectedVoting == null) {
            showAlert(AlertType.ERROR, "Kein Voting ausgewählt", "Bitte wählen Sie zuerst eine Abstimmung aus.");
            return;
        }

        String selectedOption = optionsList.getSelectionModel().getSelectedItem();
        if (selectedOption == null || selectedOption.isBlank()) {
            showAlert(AlertType.ERROR, "Keine Option ausgewählt", "Bitte wählen Sie zuerst eine Option aus.");
            return;
        }

        runAsync(
                () -> {
                    apiClient.castVote(selectedVoting.id(), selectedOption);
                    return null;
                },
                ignored -> {
                    statusLabel.setText("Stimme wurde abgegeben ✅");
                    showAlert(AlertType.INFORMATION, "Abstimmen", "Sie haben erfolgreich Ihre Stimme abgegeben.");
                }
        );
    }


    // -------------------------------------------------------
    // Async Helper: blockt nicht den JavaFX-UI-Thread
    // -------------------------------------------------------

    private interface SupplierWithException<T> {
        T get() throws Exception;
    }

    private <T> void runAsync(SupplierWithException<T> work, java.util.function.Consumer<T> onSuccess) {
        new Thread(() -> {
            try {
                T result = work.get();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Fehler: " + ex.getMessage());
                    showAlert(AlertType.ERROR, "Fehler", ex.getMessage());
                });
            }
        }).start();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
