package com.evote.app.citizen_management.ui.controller;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import com.evote.app.sharedkernel.controller.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Controller für die Registrierungsansicht.
 * Verantwortlich für die Validierung der Benutzereingaben,
 * die Durchführung der Registrierung und die Navigation zur Login-Ansicht.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RegisterController {

    @FXML private TextField firstName;

    @FXML private Label firstNameError;

    @FXML private TextField lastName;

    @FXML private Label lastNameError;

    @FXML private TextField email;

    @FXML private Label emailError;

    @FXML private PasswordField password;

    @FXML private Label passwordError;

    @FXML private Button registerButton;

    @FXML private Button loginButton;

    private final CitizenApiClient apiClient = new CitizenApiClient();

    // Regex
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 10;
    private static final String NAME_CHAR_PATTERN = "A-Za-zÄÖÜäöüß";
    private static final String NAME_REGEX =
            "^[" + NAME_CHAR_PATTERN + "]{" + MIN_NAME_LENGTH + "," + MAX_NAME_LENGTH + "}$";
    private static final String EMAIL_REGEX = "^(.+)@(\\S+)$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    /**
     * Wird beim Klick auf den Registrieren-Button ausgeführt.
     * Validiert die Eingabefelder und führt bei Erfolg die Registrierung aus.
     *
     * @param e1 ActionEvent des Buttons
     */
    @FXML
    private void registerAction(ActionEvent e1) {
        if (isAnyFieldInvalid()) return;

        String first = firstName.getText();
        String last = lastName.getText();
        String mail = email.getText();
        String pw = password.getText();

        boolean success = apiClient.registerCitizen(first, last, mail, pw);

        if (success) {
            showAlert(
                    "Registrierung",
                    "Sie haben sich erfolgreich registriert. Nun können Sie sich einloggen.",
                    AlertType.INFORMATION);
            MainController.getInstance().changeView("login");
        } else {
            showAlert("Registrierung", "Registrierung fehlgeschlagen!", AlertType.ERROR);
        }
    }

    /**
     * Wechselt zur Login-Ansicht.
     *
     * @param e2 ActionEvent des Buttons
     */
    @FXML
    private void loginAction(ActionEvent e2) {
        MainController.getInstance().changeView("login");
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
     * Validiert ein einzelnes Eingabefeld anhand eines regulären Ausdrucks
     * und zeigt bei Fehlern eine entsprechende Fehlermeldung an.
     *
     * @param field das zu prüfende TextField
     * @param errorLabel Label zur Anzeige der Fehlermeldung
     * @param regex Validierungs-RegEx
     * @param emptyMsg Fehlermeldung bei leerem Feld
     * @param invalidMsg Fehlermeldung bei ungültigem Inhalt
     * @return true, wenn das Feld ungültig ist, sonst false
     */
    private boolean validateField(
            TextField field, Label errorLabel, String regex, String emptyMsg, String invalidMsg) {

        String text = field.getText();
        boolean hasError = false;

        if (text.isEmpty()) {
            field.setStyle("-fx-border-color: #CD2626");
            errorLabel.setText(emptyMsg);
            errorLabel.setVisible(true);
            hasError = true;
        } else if (!text.matches(regex)) {
            field.setStyle("-fx-border-color: #CD2626");
            errorLabel.setText(invalidMsg);
            errorLabel.setVisible(true);
            hasError = true;
        } else {
            field.setStyle("");
            errorLabel.setVisible(false);
        }

        return hasError; // KEIN early-return! nur Status zurückgeben
    }

    /**
     * Prüft alle Eingabefelder auf Gültigkeit.
     *
     * @return true, wenn mindestens ein Feld ungültig ist, sonst false
     */
    private boolean isAnyFieldInvalid() {

        boolean firstInvalid =
                validateField(
                        firstName,
                        firstNameError,
                        NAME_REGEX,
                        "Vorname darf nicht leer sein!",
                        "Ungültiger Vorname: Nur Buchstaben, 3-10 Zeichen");

        boolean lastInvalid =
                validateField(
                        lastName,
                        lastNameError,
                        NAME_REGEX,
                        "Nachname darf nicht leer sein!",
                        "Ungütiger Name: Nur Buchstaben, 3-10 Zeichen");

        boolean emailInvalid =
                validateField(
                        email,
                        emailError,
                        EMAIL_REGEX,
                        "Email darf nicht leer sein!",
                        "Ungültige E-Mail: @ muss vorhanden sein");

        boolean passwordInvalid =
                validateField(
                        password,
                        passwordError,
                        PASSWORD_REGEX,
                        "Passwort darf nicht leer sein!",
                        "Ungültiges Passwort: 8 Zeichen, Buchstaben, Zahlen");

        return firstInvalid || lastInvalid || emailInvalid || passwordInvalid;
    }
}
