package com.evote.app.citizen_management.ui.controller;

import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.sharedkernel.controller.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * JavaFX-Controller für die Login-Ansicht.
 * Der Controller verarbeitet die Benutzereingaben zur Anmeldung,
 * prüft die Pflichtfelder auf Vollständigkeit und kommuniziert mit dem
 * CitizenApiClient, um den Login im Backend durchzuführen. Bei erfolgreicher Anmeldung
 * wird das Authentifizierungs-Token in der AuthSession
 * gespeichert und zur entsprechenden Ansicht navigiert.
 *
 * @author Lydia Boes, Fabian Schmitz
 * @version 1.0
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class LoginController {
    /**
     * Textfeld zur Eingabe der E-Mail-Adresse.
     */
    @FXML
    private TextField email;

    /**
     * Label zur Anzeige von Validierungsfehlern für das E-Mail-Feld.
     */
    @FXML
    private Label emailError;

    /**
     * Passwortfeld zur Eingabe des Passworts.
     */
    @FXML
    private PasswordField password;

    /**
     * Label zur Anzeige von Validierungsfehlern für das Passwort-Feld.
     */
    @FXML
    private Label passwordError;

    /**
     * Button zum Auslösen des Login-Vorgangs.
     */
    @FXML
    private Button loginbutton;

    /**
     * Button zum Navigieren zur Registrierungsansicht.
     */
    @FXML
    private Button registerbutton;

    /**
     * API-Client für den Login.
     */
    private final CitizenApiClient apiClient = new CitizenApiClient();

    /**
     * Session-Objekt zum Speichern des Authentifizierungs-Tokens für die weitere Nutzung.
     */
    private final AuthSession authSession;

    /**
     * Erstellt einen neuen {@code LoginController} mit der gegebenen AuthSession
     *
     * @param authSession die Session zur Speicherung des Tokens nach erfolgreichem Login
     */
    public LoginController(AuthSession authSession) {
        this.authSession = authSession;
    }

    /**
     * Event-Handler für den Login-Button.
     *
     * Zunächst werden die Eingabefelder auf Vollständigkeit geprüft.
     * Ist die Validierung erfolgreich, wird der Login-Vorgang über den
     * CitizenApiClient ausgeführt. Bei erfolgreicher Authentifizierung
     * wird das erhaltene Token in der AuthSession gespeichert und zur
     * Ansicht "vote-view" gewechselt. Schlägt der Login fehl, wird eine
     * entsprechende Fehlermeldung angezeigt.
     *
     * @param e1 das auslösende ActionEvent
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
            showAlert("Login", "Der Login ist fehlgeschlagen. Bitte überprüfen Sie ihre Eingaben.", AlertType.ERROR);
        }
    }

    /**
     * Event-Handler für den Registrierungs-Button.
     * Navigiert zur Registrierungsansicht
     *
     * @param e2 das auslösende ActionEvent
     */
    @FXML
    private void registerAction(ActionEvent e2) {
        MainController.getInstance().changeView("register");
    }


    /**
     * Zeigt einen Dialog (Alert) mit Titel, Nachricht und Typ an.
     *
     * @param title   der Fenstertitel des Alerts
     * @param message die anzuzeigende Nachricht
     * @param type    der AlertType (z. B. INFORMATION oder ERROR)
     */
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Prüft alle Felder über checkField (delegiert) und fasst die Ergebnisse zusammen.
     *
     * @return true wenn mindestens ein Pflichtfeld leer ist,
     *         andernfalls false
     */
    private boolean isAnyFieldEmpty() {

        boolean emailEmpty = checkField(email, emailError, "Email darf nicht leer sein!");
        boolean passwordEmpty = checkField(password, passwordError, "Passwort darf nicht leer sein!");

        return emailEmpty || passwordEmpty;
    }

    /**
     * Prüft, ob ein Eingabefeld leer ist.
     *
     * Bei einem leeren Feld wird eine Fehlermeldung gesetzt und das
     * Feld wird rot markiert. Ist das Feld nicht leer, wird die Fehlermeldung
     * entfernt und die Farbe zurückgesetzt.
     *
     * @param field        das zu prüfende Eingabefeld
     * @param errorLabel   das Label zur Anzeige der Fehlermeldung
     * @param errorMessage die anzuzeigende Fehlermeldung bei leerem Feld
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