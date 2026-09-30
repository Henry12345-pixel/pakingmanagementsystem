package com.ucc.parkingsystem.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.ucc.parkingsystem.model.ParkingRecord;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ParkingRecordDAO {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Creates a parking record AND marks the slot occupied, as one transaction.
    // Returns true if the entry was recorded, or false if the slot was not
// available or did not match the vehicle type.
    // Returns true if the entry was recorded, or false if the slot was not
// available or did not match the vehicle type.
    public static boolean recordEntry(String plateNumber, int typeId, int slotId)
            throws SQLException {

        // Only succeeds if the slot is still AVAILABLE and is the right type.
        String claimSql = """
        UPDATE parking_slots SET status = 'OCCUPIED'
        WHERE slot_id = ? AND type_id = ? AND status = 'AVAILABLE'
        """;
        String insertSql = """
        INSERT INTO parking_records (plate_number, type_id, slot_id, entry_time)
        VALUES (?, ?, ?, ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int rowsChanged;
            try (PreparedStatement claimPs = conn.prepareStatement(claimSql)) {
                claimPs.setInt(1, slotId);
                claimPs.setInt(2, typeId);
                rowsChanged = claimPs.executeUpdate();
            }

            if (rowsChanged == 0) {   // slot taken, or wrong type
                conn.rollback();
                return false;
            }

            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, plateNumber);
                insertPs.setInt(2, typeId);
                insertPs.setInt(3, slotId);
                insertPs.setString(4, LocalDateTime.now().format(TIME_FORMAT));
                insertPs.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;

        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Every record with no exit time yet, meaning the vehicle is still parked.
    public static List<ParkingRecord> getParkedVehicles() throws SQLException {
        String sql = """
        SELECT r.record_id, r.plate_number, v.type_name, s.slot_number,
               r.entry_time, r.exit_time
        FROM parking_records r
        JOIN vehicle_types v ON r.type_id = v.type_id
        JOIN parking_slots s ON r.slot_id = s.slot_id
        WHERE r.exit_time IS NULL
        ORDER BY r.entry_time
        """;

        List<ParkingRecord> parked = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                parked.add(new ParkingRecord(
                        rs.getInt("record_id"),
                        rs.getString("plate_number"),
                        rs.getString("type_name"),
                        rs.getString("slot_number"),
                        rs.getString("entry_time"),
                        rs.getString("exit_time")));   // always null here
            }
        }
        return parked;
    }

    // Records the exit and frees the slot, as one transaction.
// Returns false if this record already had an exit time.
    // Records the exit and frees the slot, as one transaction.
// Returns false if this record already had an exit time.
    public static boolean recordExit(int recordId) throws SQLException {

        // "AND exit_time IS NULL" means it only works once per record.
        String exitSql = """
        UPDATE parking_records SET exit_time = ?
        WHERE record_id = ? AND exit_time IS NULL
        """;
        String freeSql = """
        UPDATE parking_slots SET status = 'AVAILABLE'
        WHERE slot_id = (SELECT slot_id FROM parking_records WHERE record_id = ?)
        """;

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int rowsChanged;
            try (PreparedStatement exitPs = conn.prepareStatement(exitSql)) {
                exitPs.setString(1, LocalDateTime.now().format(TIME_FORMAT));
                exitPs.setInt(2, recordId);
                rowsChanged = exitPs.executeUpdate();
            }

            if (rowsChanged == 0) {   // someone already recorded this exit
                conn.rollback();
                return false;
            }

            try (PreparedStatement freePs = conn.prepareStatement(freeSql)) {
                freePs.setInt(1, recordId);
                freePs.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;

        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

}