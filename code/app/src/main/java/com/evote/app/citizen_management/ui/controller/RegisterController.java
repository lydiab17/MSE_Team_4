package com.evote.app.citizen_management.ui.controller;

import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RegisterController {

    @FXML
    private TextField firstName;

    @FXML
    private Label firstNameError;

    @FXML
    private TextField lastName;

    @FXML
    private Label lastNameError;

    @FXML
    private TextField email;

    @FXML
    private Label emailError;

    @FXML
    private PasswordField password;

    @FXML
    private Label passwordError;

    @FXML
    private Button registerButton;

    @FXML
    private Button loginButton;

    private final CitizenApiClient apiClient = new CitizenApiClient();

    @FXML
    private void registerAction(ActionEvent e1) {
        if (isAnyFieldEmpty()) return;

        String first = firstName.getText();
        String last  = lastName.getText();
        String mail  = email.getText();
        String pw    = password.getText();

        boolean success = apiClient.registerCitizen(first, last, mail, pw);

        if (success) {
            showAlert("Registrierung", "Erfolgreich registriert!", AlertType.CONFIRMATION);
            MainController.getInstance().changeView("login");
        } else {
            showAlert("Registrierung", "Registrierung fehlgeschlagen!", AlertType.ERROR);
        }
    }

    @FXML
    private void loginAction(ActionEvent e2) {
        MainController.getInstance().changeView("login");
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isAnyFieldEmpty() {
        boolean firstEmpty = checkField(firstName, firstNameError, "Vorname darf nicht leer sein!");
        boolean lastEmpty = checkField(lastName, lastNameError, "Nachname darf nicht leer sein!");
        boolean emailEmpty = checkField(email, emailError, "Email darf nicht leer sein!");
        boolean passwordEmpty = checkField(password, passwordError, "Passwort darf nicht leer sein!");

        return firstEmpty || lastEmpty || emailEmpty || passwordEmpty;
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
