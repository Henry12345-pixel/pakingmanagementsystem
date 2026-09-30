package com.ucc.parkingsystem.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // "jdbc:sqlite:" means "use the SQLite driver".
    // "parking.db" is the file name. It will be created in your project folder.
    private static final String URL = "jdbc:sqlite:parking.db";

    // Every other class calls this method when it needs the database.
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);

        // SQLite ignores foreign keys unless you turn them on for each connection.
        // We will use foreign keys in Step 6, so we switch them on here.
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }


}