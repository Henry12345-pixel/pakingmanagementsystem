package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.UserDAO;
import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.shape.SVGPath;
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
    @FXML private Button showPasswordButton;
    @FXML private SVGPath eyeIcon;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        // Keep the hidden and visible password fields in sync.
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());

        // Keep the text field behind the eye hidden until the user requests it.
        passwordTextField.setVisible(false);
        passwordTextField.setManaged(false);
        updateEyeIcon(false);

        // Show the logo only if it actually loads; otherwise it stays
        // invisible/unmanaged (set in FXML) and takes up no space.
        try (InputStream logoStream = getClass().getResourceAsStream("/com/ucc/parkingsystem/logo.png")) {
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
    private void onTogglePassword() {
        boolean show = !passwordTextField.isVisible();
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        passwordTextField.setVisible(show);
        passwordTextField.setManaged(show);
        updateEyeIcon(show);
        showPasswordButton.setAccessibleText(show ? "Hide password" : "Show password");
        if (show) {
            passwordTextField.requestFocus();
            passwordTextField.positionCaret(passwordTextField.getText().length());
        } else {
            passwordField.requestFocus();
            passwordField.positionCaret(passwordField.getText().length());
        }
    }

    private void updateEyeIcon(boolean visible) {
        // Eye when hidden; crossed-out eye when visible.
        eyeIcon.setContent(visible
                ? "M1 1 L15 15 M1 9 C4 3 12 3 15 9 C12 15 4 15 1 9 Z M8 6 A3 3 0 1 0 8 12 A3 3 0 1 0 8 6 Z"
                : "M1 9 C4 3 12 3 15 9 C12 15 4 15 1 9 Z M8 6 A3 3 0 1 0 8 12 A3 3 0 1 0 8 6 Z");
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