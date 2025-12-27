package com.evote.app.citizen_management.ui.controller;

import com.evote.app.sharedkernel.security.AuthSession;
import com.evote.app.ui.controller.MainController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.evote.app.citizen_management.infrastructure.CitizenApiClient;
import javafx.scene.control.Alert.AlertType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
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
    private final AuthSession authSession;

    public LoginController(AuthSession authSession) {
        this.authSession = authSession;
    }

    @FXML
    private void loginAction(ActionEvent e1) {

        if (isAnyFieldEmpty()) {
            System.out.println("Es wurden nicht alle Felder ausgefüllt.");
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
