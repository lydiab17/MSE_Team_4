package com.evote.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class HelloApplication extends Application {

    private ConfigurableApplicationContext springContext;

    public static final String APP_TITLE = "eVote";

    @Override
    public void init() {
        // Spring Boot starten und ApplicationContext erstellen
        springContext =
                new SpringApplicationBuilder(EvoteSpringConfig.class)
                        .run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Hauptansicht laden
            Node main = loadFXML("fxml/main.fxml");
            // Neue Szene erstellen mit der Hauptansicht
            Scene scene = new Scene((Parent) main, 1024, 768);
            primaryStage.setScene(scene);

            // Titel des Hauptfensters setzen
            primaryStage.setTitle(APP_TITLE);

            // Minimale Breite und Höhe des Hauptfensters setzen
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(400);

            primaryStage.show();

            // es fehlt:
            // fxmlLoader.setControllerFactory(springContext::getBean);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // FXML-Datei laden und die darin definierten Benutzeroberflächenelemente erstellen
    public static Node loadFXML(String fxmlFilename) {
        try {
            return FXMLLoader.load(HelloApplication.class.getClassLoader().getResource(fxmlFilename));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
