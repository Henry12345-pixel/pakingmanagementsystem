package com.ucc.parkingsystem.database;

import com.ucc.parkingsystem.model.ParkingSlot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;

public class ParkingSlotDAO {

    // Returns every slot, with the plate number of the vehicle currently in it.
    public static List<ParkingSlot> getAllSlots() throws SQLException {

        String sql = """
            SELECT s.slot_id, s.slot_number, v.type_name, s.status, r.plate_number
            FROM parking_slots s
            JOIN vehicle_types v ON s.type_id = v.type_id
            LEFT JOIN parking_records r
                   ON r.slot_id = s.slot_id AND r.exit_time IS NULL
            ORDER BY s.slot_number
            """;

        List<ParkingSlot> slots = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {   // one loop turn per row
                slots.add(new ParkingSlot(
                        rs.getInt("slot_id"),
                        rs.getString("slot_number"),
                        rs.getString("type_name"),
                        rs.getString("status"),
                        rs.getString("plate_number")));   // null if no vehicle
            }
        }
        return slots;
    }
    // Used to fill the vehicle-type dropdown on the Add/Edit form.
    public static List<String> getAllVehicleTypeNames() throws SQLException {
        List<String> types = new ArrayList<>();
        String sql = "SELECT type_name FROM vehicle_types ORDER BY type_id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(rs.getString("type_name"));
            }
        }
        return types;
    }

    // Adds a new slot. slotNumber must be unique (enforced by the table itself).
    public static void addSlot(String slotNumber, String typeName) throws SQLException {
        String sql = """
        INSERT INTO parking_slots (slot_number, type_id, status)
        VALUES (?, (SELECT type_id FROM vehicle_types WHERE type_name = ?), 'AVAILABLE')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
            ps.setString(2, typeName);
            ps.executeUpdate();
        }
    }

    // Updates an existing slot's number and type.
    public static void updateSlot(int slotId, String slotNumber, String typeName) throws SQLException {
        String sql = """
        UPDATE parking_slots
        SET slot_number = ?, type_id = (SELECT type_id FROM vehicle_types WHERE type_name = ?)
        WHERE slot_id = ?
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
            ps.setString(2, typeName);
            ps.setInt(3, slotId);
            ps.executeUpdate();
        }
    }

    // Deletes a slot. Throws SQLException if it has parking history (foreign key).
    public static void deleteSlot(int slotId) throws SQLException {
        String sql = "DELETE FROM parking_slots WHERE slot_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.executeUpdate();
        }
    }
    // Available slots for one vehicle type. Used by the Vehicle Entry screen.
    public static List<ParkingSlot> getAvailableSlotsByType(String typeName) throws SQLException {
        String sql = """
        SELECT s.slot_id, s.slot_number, v.type_name, s.status
        FROM parking_slots s
        JOIN vehicle_types v ON s.type_id = v.type_id
        WHERE v.type_name = ? AND s.status = 'AVAILABLE'
        ORDER BY s.slot_number
        """;

        List<ParkingSlot> slots = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typeName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    slots.add(new ParkingSlot(
                            rs.getInt("slot_id"),
                            rs.getString("slot_number"),
                            rs.getString("type_name"),
                            rs.getString("status"),
                            null));   // no vehicle in it yet, since it's available
                }
            }
        }
        return slots;
    }
}