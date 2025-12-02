package com.evote.app.citizen_management.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import javafx.scene.control.Alert.AlertType;

public class LoginController {
    @FXML
    private TextField email;

    @FXML
    private Label emailError;

    @FXML
    private PasswordField password;

    @FXML
    private Label passwordError;

    @FXML
    private Button loginbutton;

    @FXML
    private Button registerbutton;

    private final CitizenApiClient apiClient = new CitizenApiClient();

    @FXML
    private void loginAction(ActionEvent e1) {

        if (isAnyFieldEmpty()) {
            System.out.println("Es wurden nicht alle Felder ausgefüllt.");
        } else {
            System.out.println("Es wurden alle Felder ausgefüllt.");
        }

        String mail = email.getText();
        String pw = password.getText();

        boolean success = apiClient.loginCitizen(mail, pw);

        if (success) {
            showAlert("Login", "Erfolgreich eingeloggt!", AlertType.CONFIRMATION);
        } else {
            showAlert("Login", "Login fehlgeschlagen!", AlertType.ERROR);
        }

    }

    @FXML
    private void registerAction(ActionEvent e2) {
        MainController.getInstance().changeView("register");
    }


    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isAnyFieldEmpty() {

        boolean emailEmpty = checkField(email, emailError, "Email darf nicht leer sein!");
        boolean passwordEmpty = checkField(password, passwordError, "Passwort darf nicht leer sein!");

        return emailEmpty || passwordEmpty;
    }

    private boolean checkField(TextField field, Label errorLabel, String errorMessage) {
        boolean empty = field.getText().isEmpty();

        if (empty) {
            field.setStyle("-fx-border-color: red;");
            errorLabel.setText(errorMessage);
            errorLabel.setVisible(true);
        } else {
            field.setStyle("");
            errorLabel.setVisible(false);
        }
        return empty;
    }
}
