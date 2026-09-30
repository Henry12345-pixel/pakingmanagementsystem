package com.ucc.parkingsystem.model;

// Remembers who is logged in. "static" means there is one shared copy for the whole app.
public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}