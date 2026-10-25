package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.UserDAO;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class LoginController {

    @FXML private ImageView logoView;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // Keep the hidden and visible password fields in sync.
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        showPasswordCheck.selectedProperty().addListener((obs, wasShown, showNow) -> {
            passwordField.setVisible(!showNow);
            passwordField.setManaged(!showNow);
            passwordTextField.setVisible(showNow);
            passwordTextField.setManaged(showNow);
        });

        // Show the logo only if it actually loads; otherwise it stays
        // invisible/unmanaged (set in FXML) and takes up no space.
        try (InputStream logoStream = getClass().getResourceAsStream("logo.png")) {
            if (logoStream != null) {
                logoView.setImage(new Image(logoStream));
                logoView.setVisible(true);
                logoView.setManaged(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter your username and password.");
            return;
        }

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