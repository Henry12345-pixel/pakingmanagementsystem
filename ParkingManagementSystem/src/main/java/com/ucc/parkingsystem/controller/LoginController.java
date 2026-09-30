package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.UserDAO;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;


import java.io.IOException;
import java.sql.SQLException;

public class LoginController {

    // These names must match the fx:id values in login-view.fxml
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter your username and password.");
            return;
        }

        // Ask the database
        User user;
        try {
            user = UserDAO.login(username, password);
        } catch (SQLException e) {
            messageLabel.setText("Database error. Please try again.");
            e.printStackTrace();
            return;
        }

        if (user == null) {
            messageLabel.setText("Invalid username or password.");
            return;
        }

        // Remember who logged in, then open the right dashboard for their role
        Session.setCurrentUser(user);

        try {
            if (user.isAdmin()) {
                Navigator.goToAdminOnly(usernameField, "admin-dashboard-view.fxml", "Admin Dashboard", 900, 600);
            } else {
                Navigator.goTo(usernameField, "staff-dashboard-view.fxml", "Staff Dashboard", 900, 600);
            }
        } catch (IOException e) {
            messageLabel.setText("Could not open the dashboard.");
            e.printStackTrace();
        }
    }


}