package com.ucc.parkingsystem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {

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

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS roles (
                role_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                role_name TEXT NOT NULL UNIQUE
            )
            """);

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

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS vehicle_types (
                type_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                type_name TEXT NOT NULL UNIQUE
            )
            """);

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

        stmt.execute("""
            CREATE UNIQUE INDEX IF NOT EXISTS idx_one_open_record_per_slot
            ON parking_records (slot_id) WHERE exit_time IS NULL
            """);
    }

    private static void insertStartingData(Statement stmt) throws SQLException {

        stmt.execute("INSERT OR IGNORE INTO roles (role_name) VALUES ('ADMIN')");
        stmt.execute("INSERT OR IGNORE INTO roles (role_name) VALUES ('STAFF')");

        // Vehicle types: 2-Wheel is now split by engine size.
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('2-Wheel (Above 400cc)')");
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('2-Wheel (Below 400cc)')");
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('3-Wheel')");
        stmt.execute("INSERT OR IGNORE INTO vehicle_types (type_name) VALUES ('4-Wheel')");

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

        /// Sample slots, matching the floor rules:
        // Below 400cc / 3-Wheel -> Ground only.
        // Above 400cc / 4-Wheel -> Upper/Lower only (with each other).
        insertSlot(stmt, "M-01", "2-Wheel (Below 400cc)", "GROUND");
        insertSlot(stmt, "M-02", "2-Wheel (Below 400cc)", "GROUND");
        insertSlot(stmt, "BB-01", "2-Wheel (Above 400cc)", "UPPER");
        insertSlot(stmt, "BB-02", "2-Wheel (Above 400cc)", "LOWER");

        insertSlot(stmt, "T-01", "3-Wheel", "GROUND");

        insertSlot(stmt, "C-01", "4-Wheel", "UPPER");
        insertSlot(stmt, "C-02", "4-Wheel", "LOWER");
        insertSlot(stmt, "C-03", "4-Wheel", "UPPER");
    }

    private static void insertSlot(Statement stmt, String slotNumber, String typeName, String floorLevel)
            throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO parking_slots (slot_number, type_id, floor_level) "
                + "VALUES ('" + slotNumber + "', "
                + "(SELECT type_id FROM vehicle_types WHERE type_name = '" + typeName + "'), "
                + "'" + floorLevel + "')");
    }
}