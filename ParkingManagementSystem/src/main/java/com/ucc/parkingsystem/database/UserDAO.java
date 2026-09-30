package com.ucc.parkingsystem.database;

import com.ucc.parkingsystem.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Returns the User if the login is correct, or null if it is not.
    public static User login(String username, String password) throws SQLException {

        String sql = """
            SELECT u.user_id, u.username, u.full_name, r.role_name
            FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE u.username = ? AND u.password = ? AND u.is_active = 1
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);   // fills the first ?
            ps.setString(2, password);   // fills the second ?

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {   // a row came back, so the login is correct
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role_name"));
                }
            }
        }
        return null;   // no matching row
    }
    // Returns every STAFF account (not Admins), for the Manage Staff screen.
    public static List<User> getAllStaff() throws SQLException {
        String sql = """
        SELECT u.user_id, u.username, u.full_name, r.role_name, u.is_active
        FROM users u
        JOIN roles r ON u.role_id = r.role_id
        WHERE r.role_name = 'STAFF'
        ORDER BY u.full_name
        """;

        List<User> staff = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                staff.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("full_name"),
                        rs.getString("role_name"),
                        rs.getInt("is_active") == 1));
            }
        }
        return staff;
    }

    // Creates a new staff account. Username must be unique.
    public static void addStaff(String username, String password, String fullName) throws SQLException {
        String sql = """
        INSERT INTO users (username, password, full_name, role_id, is_active)
        VALUES (?, ?, ?, (SELECT role_id FROM roles WHERE role_name = 'STAFF'), 1)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.executeUpdate();
        }
    }

    // Sets a new password for an existing user.
    public static void resetPassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    // Turns an account on or off. A deactivated user can't log in (see Step 8's login SQL).
    public static void setActive(int userId, boolean active) throws SQLException {
        String sql = "UPDATE users SET is_active = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }
}