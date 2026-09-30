package com.ucc.parkingsystem.model;

public class User {
    private final int userId;
    private final String username;
    private final String fullName;
    private final String role;
    private final boolean active;   // new

    // Existing 4-argument constructor, used at login (active users only, so always true)
    public User(int userId, String username, String fullName, String role) {
        this(userId, username, fullName, role, true);
    }

    // New constructor, used by getAllStaff, which needs the real active value
    public User(int userId, String username, String fullName, String role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }

    public boolean isAdmin() {
        return role.equals("ADMIN");
    }
}