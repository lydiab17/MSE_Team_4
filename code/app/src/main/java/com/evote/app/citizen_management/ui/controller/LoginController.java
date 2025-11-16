package com.evote.app.citizen_management.ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField email;

    @FXML
    private PasswordField password;

    @FXML
    private Button loginbutton;

    @FXML
    private Button registerbutton;

    @FXML
    private void loginAction(ActionEvent e1) {

    }

    @FXML
    private void registerAction(ActionEvent e2) {
        // Wechsel des Fensters
    }

    // Ausgabe einer Erfolgsmeldung
    private void showErrorAlert(String fehlermeldung) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(fehlermeldung);
        alert.showAndWait();
    }

    // Ausgabe einer Fehlermeldung
    private void showSuccessAlert(String meldung) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Erfolg");
        alert.setHeaderText(null);
        alert.setContentText(meldung);
        alert.showAndWait();
    }
}
