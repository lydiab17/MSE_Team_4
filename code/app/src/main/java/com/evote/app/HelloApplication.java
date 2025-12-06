package com.evote.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class HelloApplication extends Application {

    // Wichtig: static, damit loadFXML (static) darauf zugreifen kann
    private static ConfigurableApplicationContext springContext;

    public static final String APP_TITLE = "eVote";

    @Override
    public void init() {
        // Spring Boot starten und ApplicationContext erstellen
        springContext =
                new SpringApplicationBuilder(EvoteSpringConfig.class)
                        .run();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Hauptansicht laden
            Parent main = loadFXML("fxml/main.fxml");

            // Neue Szene erstellen mit der Hauptansicht
            Scene scene = new Scene(main, 1024, 768);
            primaryStage.setScene(scene);

            // Titel des Hauptfensters setzen
            primaryStage.setTitle(APP_TITLE);

            // Minimale Breite und Höhe des Hauptfensters setzen
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FXML-Datei laden und den Controller über Spring erzeugen
    public static Parent loadFXML(String fxmlFilename) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    HelloApplication.class.getClassLoader().getResource(fxmlFilename)
            );

            // <<< WICHTIG: Hier binden wir Spring ein
            loader.setControllerFactory(springContext::getBean);

            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden von FXML: " + fxmlFilename, e);
        }
    }

    @Override
    public void stop() {
        // Spring-Kontext sauber herunterfahren
        if (springContext != null) {
            springContext.close();
        }
    }
}
