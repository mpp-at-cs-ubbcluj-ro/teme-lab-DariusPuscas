module TemaLab4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.context;
    requires spring.core;
    requires org.apache.logging.log4j;
    requires java.sql;

    opens start to spring.core, javafx.fxml;
    opens start.ctrl to javafx.fxml;
    opens start.services to javafx.fxml;

    exports start;
    exports start.ctrl;
    exports start.model;
    exports start.repository;
    exports start.services;
}
