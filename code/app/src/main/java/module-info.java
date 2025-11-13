module com.evote.app {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.evote.app to javafx.fxml;
    exports com.evote.app;
}