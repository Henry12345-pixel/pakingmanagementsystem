package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.database.UserDAO;
import com.ucc.parkingsystem.model.User;
import com.ucc.parkingsystem.util.PasswordPolicy;
import com.ucc.parkingsystem.util.Ui;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ManageStaffController {

    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private Label passwordHintLabel;

    @FXML private Label messageLabel;

    @FXML private TableView<User> staffTable;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, Void> actionColumn;

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getFullName()));
        usernameColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername()));
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));

        setUpActionColumn();

        wireShowPassword(passwordField, passwordTextField, showPasswordCheck);

        // Live checklist under the password box while an account is being created.
        Ui.muted(passwordHintLabel, PasswordPolicy.REQUIREMENTS);
        passwordField.textProperty().addListener((obs, oldText, newText) -> updatePasswordHint(newText));

        loadStaff();
    }

    // The buttons at the right end of every staff row:
    //   active account   -> [Change Password] [Deactivate]
    //   inactive account -> [Activate] only (no Change Password until it is activated again)
    private void setUpActionColumn() {
        actionColumn.setSortable(false);
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button changePasswordButton = new Button("Change password");
            private final Button deactivateButton = new Button("Deactivate");
            private final Button activateButton = new Button("Activate");
            private final HBox buttons = new HBox(8);

            {
                changePasswordButton.getStyleClass().addAll("button-outline", "button-small");
                deactivateButton.getStyleClass().addAll("button-danger", "button-small");
                activateButton.getStyleClass().addAll("button-primary", "button-small");
                buttons.setAlignment(Pos.CENTER_RIGHT);

                changePasswordButton.setOnAction(event -> {
                    User staff = rowUser();
                    if (staff != null) {
                        onChangePasswordClick(staff);
                    }
                });
                deactivateButton.setOnAction(event -> {
                    User staff = rowUser();
                    if (staff != null) {
                        setActive(staff, false);
                    }
                });
                activateButton.setOnAction(event -> {
                    User staff = rowUser();
                    if (staff != null) {
                        setActive(staff, true);
                    }
                });
                setAlignment(Pos.CENTER_RIGHT);
            }

            private User rowUser() {
                int index = getIndex();
                if (index < 0 || index >= getTableView().getItems().size()) {
                    return null;
                }
                return getTableView().getItems().get(index);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                User staff = empty ? null : rowUser();
                if (staff == null) {
                    setGraphic(null);
                    return;
                }
                if (staff.isActive()) {
                    buttons.getChildren().setAll(changePasswordButton, deactivateButton);
                } else {
                    buttons.getChildren().setAll(activateButton);
                }
                setGraphic(buttons);
            }
        });
    }

    private void wireShowPassword(PasswordField hidden, TextField shown, CheckBox toggle) {
        shown.textProperty().bindBidirectional(hidden.textProperty());
        toggle.selectedProperty().addListener((obs, was, showNow) -> {
            hidden.setVisible(!showNow);
            hidden.setManaged(!showNow);
            shown.setVisible(showNow);
            shown.setManaged(showNow);
        });
    }

    private void updatePasswordHint(String password) {
        if (password == null || password.isEmpty()) {
            Ui.muted(passwordHintLabel, PasswordPolicy.REQUIREMENTS);
        } else if (PasswordPolicy.isValid(password)) {
            Ui.success(passwordHintLabel, "Password meets all the requirements.");
        } else {
            Ui.error(passwordHintLabel, PasswordPolicy.describeMissing(password));
        }
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
            Ui.error(messageLabel, "Fill in full name, username, and password.");
            return;
        }
        if (!PasswordPolicy.isValid(password)) {
            Ui.error(messageLabel, "Password is too weak. " + PasswordPolicy.describeMissing(password));
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
            Ui.success(messageLabel, "Staff account created for " + fullName + ".");
            fullNameField.clear();
            usernameField.clear();
            passwordField.clear();
            loadStaff();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                Ui.error(messageLabel, "Username \"" + username + "\" already exists.");
            } else {
                showError("Could not add staff.", e);
            }
        }
    }

    // Opens the Change Password pop-up for the row whose button was clicked.
    private void onChangePasswordClick(User staff) {
        Optional<String> newPassword =
                ChangePasswordDialog.show(staffTable.getScene().getWindow(), staff);

        if (newPassword.isEmpty()) {
            return;   // cancelled
        }

        try {
            UserDAO.resetPassword(staff.getUserId(), newPassword.get());
            Ui.success(messageLabel, "Password changed for " + staff.getFullName() + ".");
        } catch (SQLException e) {
            showError("Could not change the password.", e);
        }
    }

    // Turns an account on or off. A deactivated account can't log in.
    private void setActive(User staff, boolean active) {
        String action = active ? "Activate" : "Deactivate";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                action + " the account for " + staff.getFullName() + "?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        try {
            UserDAO.setActive(staff.getUserId(), active);
            loadStaff();
            Ui.success(messageLabel, staff.getFullName() + "'s account was "
                    + (active ? "activated." : "deactivated."));
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
        Ui.error(messageLabel, message);
    }
}
