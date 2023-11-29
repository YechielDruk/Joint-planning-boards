module Project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.rmi;
    requires java.sql;
    requires javafx.base;
    requires javafx.graphics;
    requires mysql.connector.j;
    requires javafx.swt;
    requires javafx.media;
    requires javafx.swing;
    requires javafx.web;

    exports users;
    opens users to javafx.fxml;
    exports server;
    opens server to java.rmi;
    exports registration;
    opens registration to javafx.fxml;
}