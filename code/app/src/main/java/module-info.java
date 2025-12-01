open module com.evote.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    // Spring Boot / Web
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.web;
    requires spring.beans;
    requires spring.core;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;



    exports com.evote.app;
}
