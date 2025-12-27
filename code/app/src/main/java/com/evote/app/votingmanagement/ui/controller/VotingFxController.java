package com.evote.app.votingmanagement.ui.controller;

import com.evote.app.ui.controller.MainController;
import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.votingmanagement.interfaces.dto.CreateVotingRequest;
import com.evote.app.votingmanagement.interfaces.dto.OptionResultResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResponse;
import com.evote.app.votingmanagement.interfaces.dto.VotingResultsResponse;
import com.evote.app.votingmanagement.ui.api.VotingApiClient;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Component;

/**
 * JavaFX-Controller für die Voting-View (Anlegen/Öffnen/Listen/Ergebnisse).
 */
@Component
public class VotingFxController {

  private final VotingApiClient apiClient;
  private final AuthSession authSession;

  private VotingResponse selectedVoting;

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
  private ListView<VotingResponse> openVotingsList;

  @FXML
  private ListView<VotingResponse> notOpenVotingsList;

  @FXML
  private Label statusLabel;

  @FXML
  private TableView<OptionResultResponse> resultsTable;

  @FXML
  private TableColumn<OptionResultResponse, String> optionColumn;

  @FXML
  private TableColumn<OptionResultResponse, Long> countColumn;

  public VotingFxController(VotingApiClient apiClient, AuthSession authSession) {
    this.apiClient = apiClient;
    this.authSession = authSession;
  }

  @FXML
  private void initialize() {
    openVotingsList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(VotingResponse item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : formatVoting(item));
      }
    });

    notOpenVotingsList.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(VotingResponse item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : formatVoting(item));
      }
    });

    // Wenn links/rechts ausgewählt wird: selectedVoting setzen + Ergebnis-Tabelle leeren
    openVotingsList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
      if (n != null) {
        notOpenVotingsList.getSelectionModel().clearSelection();
        onVotingSelected(n);
      }
    });

    notOpenVotingsList.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
      if (n != null) {
        openVotingsList.getSelectionModel().clearSelection();
        onVotingSelected(n);
      }
    });

    // TableColumns für Record-DTOs korrekt setzen
    optionColumn.setCellValueFactory(cell ->
            new ReadOnlyStringWrapper(cell.getValue().option()));
    countColumn.setCellValueFactory(cell ->
            new ReadOnlyObjectWrapper<>(cell.getValue().count()));

    refreshVotingLists();
  }

  private void onVotingSelected(VotingResponse voting) {
    selectedVoting = voting;
    resultsTable.getItems().clear();
    fillFormFromVoting(voting);
    statusLabel.setText("Ausgewählt: Voting " + voting.id());
  }

  @FXML
  private void onCreateVoting() {
    if (authSession.token().isEmpty()) {
      showError("Nicht eingeloggt", "Bitte zuerst einloggen.");
      return;
    }

    try {
      int id = parseId();
      String name = nameField.getText();
      String info = infoArea.getText();
      LocalDate start = startDatePicker.getValue();
      LocalDate end = endDatePicker.getValue();
      Set<String> options = parseOptions(optionsField.getText());

      CreateVotingRequest req = new CreateVotingRequest(
              id, name, info, start, end, options.stream().toList()
      );

      runAsync(
              () -> apiClient.createVoting(req),
              created -> {
                statusLabel.setText("Voting " + created.id() + " erfolgreich angelegt.");
                clearForm();
                refreshVotingLists();
              },
              ex -> showError("Fehler beim Anlegen", ex.getMessage())
      );



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
      VotingResponse selected = getSelectedVoting();
      if (selected == null) {
        throw new IllegalArgumentException("Bitte ein Voting in einer Liste auswählen.");
      }
      onVotingSelected(selected);
    } catch (Exception e) {
      showError("Fehler beim Laden", e.getMessage());
    }
  }

  @FXML
  private void onOpenSelectedVoting() {
    if (authSession.token().isEmpty()) {
      showError("Nicht eingeloggt", "Bitte zuerst einloggen.");
      return;
    }

    VotingResponse selected = notOpenVotingsList.getSelectionModel().getSelectedItem();
    if (selected == null) {
      showError("Fehler beim Öffnen", "Bitte ein nicht geöffnetes Voting links auswählen.");
      return;
    }

    runAsync(
            () -> {
              apiClient.openVoting(selected.id());
              return null;
            },
            ignored -> {
              statusLabel.setText("Voting " + selected.id() + " wurde geöffnet.");
              refreshVotingLists();
            },
            ex -> showError("REST-Fehler", ex.getMessage())
    );

  }

  @FXML
  private void onRefreshVotingLists() {
    refreshVotingLists();
  }

  private void refreshVotingLists() {
    runAsync(
            () -> new Lists(apiClient.getOpenVotings(), apiClient.getNotOpenVotings()),
            lists -> {
              openVotingsList.setItems(FXCollections.observableArrayList(lists.open));
              notOpenVotingsList.setItems(FXCollections.observableArrayList(lists.notOpen));
              statusLabel.setText("Listen aktualisiert.");
            },
            ex -> showError("REST-Fehler", ex.getMessage())
    );

  }

  @FXML
  private void onLoadResults() {
    VotingResponse selected = selectedVoting != null ? selectedVoting : getSelectedVoting();
    if (selected == null) {
      statusLabel.setText("Bitte zuerst ein Voting auswählen.");
      return;
    }
    selectedVoting = selected;

    runAsync(
            () -> apiClient.getResults(selectedVoting.id()),
            (VotingResultsResponse resp) -> {
              resultsTable.getItems().setAll(resp.results());
              statusLabel.setText("Ergebnisse geladen ✅");
            },
            ex -> showError("REST-Fehler", ex.getMessage())
    );

  }

  @FXML
  private void onGoToVoteView() {
    MainController.getInstance().changeView("vote-view");
  }

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

  private void clearForm() {
    idField.clear();
    nameField.clear();
    infoArea.clear();
    startDatePicker.setValue(null);
    endDatePicker.setValue(null);
    optionsField.clear();
  }

  private void fillFormFromVoting(VotingResponse voting) {
    idField.setText(String.valueOf(voting.id()));
    nameField.setText(voting.name());
    infoArea.setText(voting.info());
    startDatePicker.setValue(voting.startDate());
    endDatePicker.setValue(voting.endDate());
    optionsField.setText(voting.options().stream().collect(Collectors.joining(", ")));
  }

  private VotingResponse getSelectedVoting() {
    VotingResponse selected = notOpenVotingsList.getSelectionModel().getSelectedItem();
    if (selected != null) {
      return selected;
    }
    return openVotingsList.getSelectionModel().getSelectedItem();
  }

  private String formatVoting(VotingResponse v) {
    return String.format(
            "ID %d - %s [%s bis %s]%s",
            v.id(),
            v.name(),
            v.startDate(),
            v.endDate(),
            v.open() ? " (offen)" : ""
    );
  }

  private void showError(String title, String message) {
    statusLabel.setText("Fehler: " + message);
    Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
    alert.setTitle("Fehler");
    alert.setHeaderText(title);
    alert.showAndWait();
  }

  private <T> void runAsync(ThrowingSupplier<T> work, UiConsumer<T> onSuccess, UiConsumer<Throwable> onError) {
    Task<T> task = new Task<>() {
      @Override
      protected T call() throws Exception {
        return work.get();
      }
    };

    task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));

    task.setOnFailed(e -> {
      Throwable ex = task.getException();
      Platform.runLater(() -> onError.accept(ex));
    });

    Thread t = new Thread(task);
    t.setDaemon(true);
    t.start();
  }


  private record Lists(java.util.List<VotingResponse> open,
                       java.util.List<VotingResponse> notOpen) {
  }

  @FunctionalInterface
  private interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  @FunctionalInterface
  private interface UiConsumer<T> {
    void accept(T t);
  }
}
