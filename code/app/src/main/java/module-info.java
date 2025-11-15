module com.evote.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires spring.boot;
    requires spring.context;
    requires spring.boot.autoconfigure;

    opens com.evote.app to javafx.fxml;
    exports com.evote.app;
}