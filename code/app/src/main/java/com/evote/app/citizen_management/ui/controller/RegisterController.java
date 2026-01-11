package com.evote.app.citizen_management.ui.controller;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import com.evote.app.sharedkernel.controller.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * JavaFX-Controller für die Registrierungsansicht.
 * Dieser Controller verarbeitet die Benutzereingaben zur Registrierung
 * eines neuen Bürgers, validiert die Eingabefelder und kommuniziert mit dem
 * CitizenApiClient, um die Registrierung im Backend durchzuführen.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RegisterController {

    /** Textfeld zur Eingabe des Vornamens. */
    @FXML
    private TextField firstName;

    /** Label zur Anzeige von Validierungsfehlern für den Vornamen. */
    @FXML
    private Label firstNameError;

    /** Textfeld zur Eingabe des Nachnamens. */
    @FXML
    private TextField lastName;

    /** Label zur Anzeige von Validierungsfehlern für den Nachnamen. */
    @FXML
    private Label lastNameError;

    /** Textfeld zur Eingabe der E-Mail-Adresse. */
    @FXML
    private TextField email;

    /** Label zur Anzeige von Validierungsfehlern für die E-Mail-Adresse. */
    @FXML
    private Label emailError;

    /** Passwortfeld zur Eingabe des Passworts. */
    @FXML
    private PasswordField password;

    /** Label zur Anzeige von Validierungsfehlern für das Passwort. */
    @FXML
    private Label passwordError;

    /** Button zum Auslösen des Registrierungsvorgangs. */
    @FXML
    private Button registerButton;

    /** Button zum Wechsel zur Login-Ansicht. */
    @FXML
    private Button loginButton;

    /** API-Client zur Durchführung der Registrierung. */
    private final CitizenApiClient apiClient = new CitizenApiClient();

    // Regex
    /** Minimale erlaubte Länge für Vor- und Nachnamen. */
    private static final int MIN_NAME_LENGTH = 3;
    /** Maximale erlaubte Länge für Vor- und Nachnamen. */
    private static final int MAX_NAME_LENGTH = 10;
    /** Erlaubte Zeichen für Vor- und Nachnamen. */
    private static final String NAME_CHAR_PATTERN = "A-Za-zÄÖÜäöüß";
    /** Regulärer Ausdruck zur Validierung von Vor- und Nachnamen. */
    private static final String NAME_REGEX =
            "^[" + NAME_CHAR_PATTERN + "]{" + MIN_NAME_LENGTH + "," + MAX_NAME_LENGTH + "}$";
    /** Regulärer Ausdruck zur Validierung von E-Mail-Adressen. */
    private static final String EMAIL_REGEX = "^(.+)@(\\S+)$";
    /** Regulärer Ausdruck zur Validierung von Passwörtern. */
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";


    /**
     * Event-Handler für den Registrierungs-Button.
     *
     * Führt zunächst eine Validierung aller Eingabefelder durch.
     * Bei gültigen Eingaben wird der Registrierungsvorgang über den
     * CitizenApiClient ausgeführt. Abhängig vom Ergebnis wird
     * eine Erfolgs- oder Fehlermeldung angezeigt und ggf. zur Login-Ansicht
     * gewechselt.
     *
     *
     * @param e1 das auslösende ActionEvent
     */
    @FXML
    private void registerAction(ActionEvent e1) {
        if (isAnyFieldInvalid()) return;

        String first = firstName.getText();
        String last  = lastName.getText();
        String mail  = email.getText();
        String pw    = password.getText();

        boolean success = apiClient.registerCitizen(first, last, mail, pw);

        if (success) {
            showAlert("Registrierung", "Sie haben sich erfolgreich registriert. Nun können Sie sich einloggen.", AlertType.INFORMATION);
            MainController.getInstance().changeView("login");
        } else {
            showAlert("Registrierung", "Registrierung fehlgeschlagen!", AlertType.ERROR);
        }
    }

    /**
     * Event-Handler für den Login-Button.
     * Wechselt von der Registrierungsansicht zur Login-Ansicht.
     *
     * @param e2 das auslösende ActionEvent
     */
    @FXML
    private void loginAction(ActionEvent e2) {
        MainController.getInstance().changeView("login");
    }

    /**
     * Zeigt einen Dialog (Alert) mit dem angegebenen Titel, Text und Typ an.
     *
     * @param title   der Titel des Dialogs
     * @param message die anzuzeigende Nachricht
     * @param type    der AlertType des Dialogs
     */
    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Prüft genau ein Eingabefeld. (wiederverwendbare Einzel-Validierung)
     *
     * Ist das Feld leer oder entspricht der Inhalt nicht dem angegebenen
     * regulären Ausdruck, wird eine Fehlermeldung angezeigt und das Feld
     * rot markiert. Sollte das Feld nicht leer sein und der Inhalt entspricht
     * dem angegebenen regulären Ausdruck, wird die Fehlermeldung nicht angezeigt
     * und das Feld wird nicht rot markiert.
     *
     *
     * @param field        das zu prüfende Eingabefeld
     * @param errorLabel   das Label zur Anzeige der Fehlermeldung
     * @param regex        der reguläre Ausdruck zur Validierung
     * @param emptyMsg     die Fehlermeldung bei leerem Feld
     * @param invalidMsg   die Fehlermeldung bei ungültigem Inhalt
     * @return true, wenn das Feld ungültig ist, sonst false
     */
    private boolean validateField(TextField field, Label errorLabel, String regex,
                                  String emptyMsg, String invalidMsg) {

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

        return hasError;
    }

    /**
     * Gesamtprüfung.
     * Validiert alle Felder über validateField (delegiert) und fasst die Ergebnisse zusammen.
     *
     * @return true, wenn mindestens ein Feld ungültig ist, sonst false.
     */
    private boolean isAnyFieldInvalid() {

        boolean firstInvalid = validateField(
                firstName, firstNameError,
                NAME_REGEX,
                "Vorname darf nicht leer sein!",
                "Ungültiger Vorname: Nur Buchstaben, 3-10 Zeichen"
        );

        boolean lastInvalid = validateField(
                lastName, lastNameError,
                NAME_REGEX,
                "Nachname darf nicht leer sein!",
                "Ungütiger Name: Nur Buchstaben, 3-10 Zeichen"
        );

        boolean emailInvalid = validateField(
                email, emailError,
                EMAIL_REGEX,
                "Email darf nicht leer sein!",
                "Ungültige E-Mail: @ muss vorhanden sein"
        );

        boolean passwordInvalid = validateField(
                password, passwordError,
                PASSWORD_REGEX,
                "Passwort darf nicht leer sein!",
                "Ungültiges Passwort: 8 Zeichen, Buchstaben, Zahlen"
        );

        return firstInvalid || lastInvalid || emailInvalid || passwordInvalid;
    }



}