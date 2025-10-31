module com.example.cycleborrowingsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;
    requires java.desktop;
    requires java.sql;

    exports com.example.cycleborrowingsystem to javafx.graphics;
    opens com.example.cycleborrowingsystem.controllers to javafx.fxml;
    opens com.example.cycleborrowingsystem.net to javafx.fxml;
}
