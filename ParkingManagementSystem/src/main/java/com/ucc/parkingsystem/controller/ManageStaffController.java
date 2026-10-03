package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.UserDAO;
import com.ucc.parkingsystem.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class ManageStaffController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label messageLabel;

    @FXML private TableView<User> staffTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> statusColumn;

    private User selectedStaff;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getFullName()));
        usernameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));

        staffTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldRow, newRow) -> selectedStaff = newRow);

        loadStaff();
    }

    private void loadStaff() {
        try {
            List<User> staff = UserDAO.getAllStaff();
            staffTable.setItems(FXCollections.observableArrayList(staff));
        } catch (SQLException e) {
            showError("Could not load staff accounts.", e);
        }
    }

    @FXML
    private void onAddClick() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Fill in full name, username, and password.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Add new staff account for " + fullName + " (username: " + username + ")?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            UserDAO.addStaff(username, password, fullName);
            messageLabel.setText("");
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
            loadStaff();
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                messageLabel.setText("Username \"" + username + "\" already exists.");
            } else {
                showError("Could not add staff.", e);
            }
        }
    }
    @FXML
    private void onResetPasswordClick() {
        if (selectedStaff == null) {
            messageLabel.setText("Select a staff member from the table first.");
            return;
        }
        String newPassword = newPasswordField.getText();
        if (newPassword.isEmpty()) {
            messageLabel.setText("Type a new password first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Reset the password for " + selectedStaff.getFullName() + "?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            UserDAO.resetPassword(selectedStaff.getUserId(), newPassword);
            messageLabel.setText("Password updated for " + selectedStaff.getFullName() + ".");
            newPasswordField.clear();
        } catch (SQLException e) {
            showError("Could not reset the password.", e);
        }
    }

    @FXML
    private void onDeactivateClick() {
        setActive(false);
    }

    @FXML
    private void onActivateClick() {
        setActive(true);
    }

    private void setActive(boolean active) {
        if (selectedStaff == null) {
            messageLabel.setText("Select a staff member from the table first.");
            return;
        }

        String action = active ? "Activate" : "Deactivate";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                action + " the account for " + selectedStaff.getFullName() + "?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            UserDAO.setActive(selectedStaff.getUserId(), active);
            messageLabel.setText("");
            loadStaff();
        } catch (SQLException e) {
            showError("Could not update the account.", e);
        }
    }

    @FXML
    private void onBackClick() {
        try {
            Navigator.goToAdminOnly(fullNameField, "admin-dashboard-view.fxml",
                    "Admin Dashboard", 900, 600);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message, SQLException e) {
        e.printStackTrace();
        messageLabel.setText(message);
    }
}