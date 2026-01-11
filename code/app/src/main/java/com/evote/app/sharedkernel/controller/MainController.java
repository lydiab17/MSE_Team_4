package com.evote.app.sharedkernel.controller;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import com.evote.app.HelloApplication;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Zentraler Controller zur Steuerung der Hauptansicht der Anwendung. Hält eine statische Referenz
 * auf die aktuell initialisierte Instanz, um anderen Controllern einen globalen Zugriff für
 * View-Wechsel zu ermöglichen.
 *
 * <p>Dies ist kein klassisches Singleton-Pattern, sondern eine pragmatische Lösung im
 * JavaFX-Kontext.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class MainController {

  /** Statische Referenz auf die aktuell initialisierte MainController-Instanz. */
  private static MainController instance;

  /**
   * Liefert die aktuell initialisierte MainController-Instanz. Die Instanz wird beim Initialisieren
   * des Controllers gesetzt.
   *
   * @return aktuelle MainController-Instanz
   */
  public static MainController getInstance() {
    return instance;
  }

  /** Container, der die aktuell angezeigte View enthält. */
  @FXML StackPane viewHolder;

  /** Initialisiert den Controller und setzt die statische Referenz auf diese Instanz. */
  @FXML
  public void initialize() {
    instance = this;
  }

  /**
   * Wechselt die aktuell angezeigte View, indem die angegebene FXML-Datei geladen und im
   * View-Container angezeigt wird.
   *
   * @param fxmlFilename Name der FXML-Datei ohne Dateiendung
   */
  public void changeView(String fxmlFilename) {
    Node view = HelloApplication.loadFXML("fxml/" + fxmlFilename + ".fxml");
    viewHolder.getChildren().setAll(view);
  }
}
