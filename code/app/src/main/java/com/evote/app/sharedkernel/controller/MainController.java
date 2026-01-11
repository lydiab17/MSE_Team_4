package com.evote.app.sharedkernel.controller;

import com.evote.app.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;
/**
 * Verwaltet einen zentralen StackPane, in dem unterschiedliche
 * Views dynamisch geladen und angezeigt werden.
 *
 * @author Lydia Boes
 * @version 1.0
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class MainController {

    /**
     * Enthält die aktuell aktive Instanz des MainController.
     * Wird in initialize gesetzt.
     */
    private static MainController instance;


    /**
     * Liefert die aktuell aktive Instanz des MainController.
     *
     * @return die aktuelle MainController-Instanz oder null,
     *         falls der Controller noch nicht initialisiert wurde
     */
    public static MainController getInstance() {
        return instance;
    }

    /** Zentraler Container, in den unterschiedliche Views geladen werden */
    @FXML
    StackPane viewHolder;

    /**
     * Wird automatisch von JavaFX aufgerufen, nachdem der Controller erstellt wurde.
     * Speichert die aktuelle Instanz, damit sie über getInstance()
     * von anderen Klassen aus verwendet werden kann.
     */
    @FXML
    public void initialize() {
        instance = this;
    }

    /**
     * Wechselt die aktuell angezeigte View.
     *
     * @param fxmlFilename der Name der FXML-Datei (ohne Dateiendung),
     *                     die geladen werden soll
     */
    public void changeView(String fxmlFilename) {

        // lädt die FXML-Datei und erhält das zugehörige Node-Objekt
        Node view = HelloApplication.loadFXML("fxml/" + fxmlFilename + ".fxml");

        // Ersetzt alle vorhandenen Children durch die neue View
        viewHolder.getChildren().setAll(view);
    }
}
