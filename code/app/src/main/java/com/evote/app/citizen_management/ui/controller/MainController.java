package com.evote.app.citizen_management.ui.controller;

import com.evote.app.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class MainController {
    // Singleton
    // Sicherstellen, dass von einer Klasse genau eine einzige Instanz existiert
    // und dass diese Instanz global zugreifbar ist.
    private static MainController instance;

    // Singleton-Zugriff auf die einzige Instanz von Main-Controller
    public static MainController getInstance() {
        return instance;
    }

    // Container für Ansichten
    @FXML
    StackPane viewHolder;

    @FXML
    public void initialize() {
        // Initialisiere die Singleton-Instanz mit der aktuellen Instanz dieser Klasse
        instance = this;
    }

    // ändert die aktuelle Ansicht basierend auf dem übergebenen FXML-Dateinamen
    public void changeView(String fxmlFilename) {

        // lädt die FXML-Datei und erhält das zugehörige Node-Objekt
        Node view = HelloApplication.loadFXML("fxml/" + fxmlFilename + ".fxml");

        // alle vorhandenen Kinder des StackPane werden gelöscht und die neue Ansicht wird als einziges Kind hinzugefügt
        viewHolder.getChildren().setAll(view);
    }
}
