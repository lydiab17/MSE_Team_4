package com.evote.app.citizen_management.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import javax.swing.*;

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

    @FXML
    private void registerAction(ActionEvent e1) {
        if (isAnyFieldEmpty()) {
            System.out.println("Es wurden nicht alle Felder ausgefüllt.");
        } else {
            System.out.println("Es wurden alle Felder ausgefüllt.");
        }

    }

    @FXML
    private void loginAction(ActionEvent e2) {
        MainController.getInstance().changeView("login");
    }

    // Ausgabe einer Erfolgsmeldung
    private void showSuccessAlert(String meldung) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Erfolg");
        alert.setHeaderText(null);
        alert.setContentText(meldung);
        alert.showAndWait();
    }

    // Ausgabe einer Fehlermeldung
    private void showErrorAlert(String fehlermeldung) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(fehlermeldung);
        alert.showAndWait();
    }

    private boolean isAnyFieldEmpty() {

        boolean firstEmpty = firstName.getText().isEmpty();
        boolean lastEmpty = lastName.getText().isEmpty();
        boolean emailEmpty= email.getText().isEmpty();
        boolean passwordEmpty = password.getText().isEmpty();

        if (firstEmpty) {
            firstName.setStyle("-fx-border-color: red;");
            firstNameError.setText("Vorname darf nicht leer sein!");
            firstNameError.setVisible(true);
        } else {
            firstName.setStyle(""); // Standard-Stil zurücksetzen
            firstNameError.setVisible(false);
        }

        if (lastEmpty) {
            lastName.setStyle("-fx-border-color: red;");
            lastNameError.setText("Nachname darf nicht leer sein!");
            lastNameError.setVisible(true);
        } else {
            lastName.setStyle(""); // Standard-Stil zurücksetzen
            lastNameError.setVisible(false);
        }

        if (emailEmpty) {
            email.setStyle("-fx-border-color: red;");
            emailError.setText("Email darf nicht leer sein!");
            emailError.setVisible(true);
        } else {
            email.setStyle(""); // Standard-Stil zurücksetzen
            emailError.setVisible(false);
        }

        if (passwordEmpty) {
            password.setStyle("-fx-border-color: red;");
            passwordError.setText("Passwort darf nicht leer sein!");
            passwordError.setVisible(true);
        } else {
            password.setStyle(""); // Standard-Stil zurücksetzen
            passwordError.setVisible(false);
        }

        return firstEmpty || lastEmpty || emailEmpty || passwordEmpty;
    }
}
