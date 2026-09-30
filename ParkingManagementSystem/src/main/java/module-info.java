module com.ucc.parkingsystem {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;                 // Connection, Statement, ResultSet, etc.
    requires org.xerial.sqlitejdbc;    // the SQLite driver itself

    opens com.ucc.parkingsystem to javafx.fxml;
    opens com.ucc.parkingsystem.controller to javafx.fxml;
    exports com.ucc.parkingsystem;
}