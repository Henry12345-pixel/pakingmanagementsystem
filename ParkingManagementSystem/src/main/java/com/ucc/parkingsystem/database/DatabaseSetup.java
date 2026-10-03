package com.ucc.parkingsystem.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DatabaseSetup {

    // Called once when the app starts. Safe to run many times.
    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            createTables(stmt);
            insertStartingData(stmt);
            System.out.println("Database ready.");

        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTables(Statement stmt) throws SQLException {

        // 1. roles: ADMIN and STAFF
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS roles (
                role_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                role_name TEXT NOT NULL UNIQUE
            )
            """);

        // 2. users: Admin and Staff accounts
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                username  TEXT NOT NULL UNIQUE,
                password  TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role_id   INTEGER NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1,
                FOREIGN KEY (role_id) REFERENCES roles(role_id)
            )
            """);

        // 3. vehicle_types: 2-Wheel, 3-Wheel, 4-Wheel
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS vehicle_types (
                type_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                type_name TEXT NOT NULL UNIQUE
            )
            """);

        // 4. parking_slots: each slot is for one vehicle type AND one floor level
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS parking_slots (
                slot_id     INTEGER PRIMARY KEY AUTOINCREMENT,
                slot_number TEXT NOT NULL UNIQUE,
                type_id     INTEGER NOT NULL,
                floor_level TEXT NOT NULL DEFAULT 'GROUND'
                            CHECK (floor_level IN ('GROUND', 'UPPER', 'LOWER')),
                status      TEXT NOT NULL DEFAULT 'AVAILABLE'
                            CHECK (status IN ('AVAILABLE', 'OCCUPIED')),
                FOREIGN KEY (type_id) REFERENCES vehicle_types(type_id)
            )
            """);


        // 5. parking_records: one row per parking visit
//    exit_time stays NULL while the vehicle is still parked
        stmt.execute("""
    CREATE TABLE IF NOT EXISTS parking_records (
        record_id    INTEGER PRIMARY KEY AUTOINCREMENT,
        plate_number TEXT NOT NULL,
        type_id      INTEGER NOT NULL,
        slot_id      INTEGER NOT NULL,
        entry_time   TEXT NOT NULL,
        exit_time    TEXT,
        FOREIGN KEY (type_id) REFERENCES vehicle_types(type_id),
        FOREIGN KEY (slot_id) REFERENCES parking_slots(slot_id)
    )
    """);
    }

    private static void insertStartingData(Statement stmt) throws SQLException {

        // The two roles
        stmt.execute("INSERT OR IGNORE INTO roles (role_name) VALUES ('ADMIN')");
        stmt.execute("INSERT OR IGNORE INTO roles (role_name) VALUES ('STAFF')");

        // The three vehicle types
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('2-Wheel')");
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('3-Wheel')");
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('4-Wheel')");

        // One Admin and one Staff account so we can log in during testing
        Connection conn = stmt.getConnection();

        try (PreparedStatement ps = conn.prepareStatement("""
        INSERT OR IGNORE INTO users (username, password, full_name, role_id)
        VALUES (?, ?, 'System Administrator',
                (SELECT role_id FROM roles WHERE role_name = 'ADMIN'))
        """)) {
            ps.setString(1, "admin");
            ps.setString(2, PasswordUtil.hash("admin123"));
            ps.executeUpdate();
        }

        try (PreparedStatement ps = conn.prepareStatement("""
        INSERT OR IGNORE INTO users (username, password, full_name, role_id)
        VALUES (?, ?, 'Sample Staff',
                (SELECT role_id FROM roles WHERE role_name = 'STAFF'))
        """)) {
            ps.setString(1, "staff");
            ps.setString(2, PasswordUtil.hash("staff123"));
            ps.executeUpdate();
        }

        // Regular motorcycles: Ground floor
        insertSlot(stmt, "M-01", "2-Wheel", "GROUND");
        insertSlot(stmt, "M-02", "2-Wheel", "GROUND");

// Big bikes: parked with the cars, Upper/Lower floor
        insertSlot(stmt, "BB-01", "2-Wheel", "UPPER");
        insertSlot(stmt, "BB-02", "2-Wheel", "LOWER");

        insertSlot(stmt, "T-01", "3-Wheel", "GROUND");

// Cars spread across floors
        insertSlot(stmt, "C-01", "4-Wheel", "GROUND");
        insertSlot(stmt, "C-02", "4-Wheel", "UPPER");
        insertSlot(stmt, "C-03", "4-Wheel", "LOWER");
    }

    private static void insertSlot(Statement stmt, String slotNumber, String typeName, String floorLevel)
            throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO parking_slots (slot_number, type_id, floor_level) "
                + "VALUES ('" + slotNumber + "', "
                + "(SELECT type_id FROM vehicle_types WHERE type_name = '" + typeName + "'), "
                + "'" + floorLevel + "')");
    }


}