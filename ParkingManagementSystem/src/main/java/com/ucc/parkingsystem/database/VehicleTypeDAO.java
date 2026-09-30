package com.ucc.parkingsystem.database;

import com.ucc.parkingsystem.model.VehicleType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehicleTypeDAO {

    // Reads the vehicle types (2-Wheel, 3-Wheel, 4-Wheel) with their real IDs.
    public static List<VehicleType> getAllTypes() throws SQLException {
        String sql = "SELECT type_id, type_name FROM vehicle_types ORDER BY type_id";
        List<VehicleType> types = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(new VehicleType(rs.getInt("type_id"), rs.getString("type_name")));
            }
        }
        return types;
    }
}