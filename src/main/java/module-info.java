module com.fatec.rfidscanwave {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires mysql.connector.j;

    opens com.fatec.rfidscanwave to javafx.fxml;
    exports com.fatec.rfidscanwave;
    exports com.fatec.rfidscanwave.controller;
    opens com.fatec.rfidscanwave.controller to javafx.fxml;
    exports com.fatec.rfidscanwave.view;
    opens com.fatec.rfidscanwave.view to javafx.fxml;
}