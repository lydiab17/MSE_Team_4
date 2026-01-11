package com.evote.app.citizen_management.ui.controller;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import com.evote.app.sharedkernel.controller.MainController;
import com.evote.app.sharedkernel.security.AuthSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller für die Login-Ansicht. Verantwortlich für die Validierung der Login-Daten, die
 * Authentifizierung des Benutzers und die Navigation zu weiteren Ansichten.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class LoginController {
  @FXML private TextField email;

  @FXML private Label emailError;

  @FXML private PasswordField password;

  @FXML private Label passwordError;

  @FXML private Button loginbutton;

  @FXML private Button registerbutton;

  private final CitizenApiClient apiClient = new CitizenApiClient();
  private final AuthSession authSession;

  /**
   * Konstruktor für den LoginController.
   *
   * @param authSession Session zur Speicherung des Authentifizierungs-Tokens
   */
  public LoginController(AuthSession authSession) {
    this.authSession = authSession;
  }

  /**
   * Wird beim Klick auf den Login-Button ausgeführt. Prüft die Eingaben, führt den Login durch und
   * speichert bei Erfolg das Authentifizierungs-Token.
   *
   * @param e1 ActionEvent des Buttons
   */
  @FXML
  private void loginAction(ActionEvent e1) {

    if (isAnyFieldEmpty()) {
      return;
    }

    String mail = email.getText();
    String pw = password.getText();

    var tokenOpt = apiClient.loginAndGetToken(mail, pw);

    if (tokenOpt.isPresent()) {
      authSession.setToken(tokenOpt.get()); // <-- Token merken
      showAlert("Login", "Sie haben sich erfolgreich eingeloggt!", AlertType.INFORMATION);
      MainController.getInstance().changeView("vote-view");
    } else {
      showAlert(
          "Login",
          "Der Login ist fehlgeschlagen. Bitte überprüfen Sie ihre Eingaben.",
          AlertType.ERROR);
    }
  }

  /**
   * Wechselt zur Registrierungsansicht.
   *
   * @param e2 ActionEvent des Buttons
   */
  @FXML
  private void registerAction(ActionEvent e2) {
    MainController.getInstance().changeView("register");
  }

  /**
   * Zeigt einen modalen Alert mit Titel, Nachricht und Typ an.
   *
   * @param title Titel des Dialogs
   * @param message Inhaltstext des Dialogs
   * @param type Typ des Alerts
   */
  private void showAlert(String title, String message, AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * Prüft, ob eines der Eingabefelder leer ist (Gesamtprüfung).
   *
   * @return true, wenn mindestens ein Feld leer ist, sonst false
   */
  private boolean isAnyFieldEmpty() {

    boolean emailEmpty = checkField(email, emailError, "Email darf nicht leer sein!");
    boolean passwordEmpty = checkField(password, passwordError, "Passwort darf nicht leer sein!");

    return emailEmpty || passwordEmpty;
  }

  /**
   * Prüft ein einzelnes Eingabefeld auf leeren Inhalt und zeigt bei Bedarf eine Fehlermeldung an.
   *
   * @param field das zu prüfende TextField
   * @param errorLabel Label zur Anzeige der Fehlermeldung
   * @param errorMessage anzuzeigende Fehlermeldung
   * @return true, wenn das Feld leer ist, sonst false
   */
  private boolean checkField(TextField field, Label errorLabel, String errorMessage) {
    boolean empty = field.getText().isEmpty();

    if (empty) {
      field.setStyle("-fx-border-color: #CD2626;");
      errorLabel.setText(errorMessage);
      errorLabel.setVisible(true);
    } else {
      field.setStyle("");
      errorLabel.setVisible(false);
    }
    return empty;
  }
}
