package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;
import java.io.IOException;

// Changes the screen shown in the window. Every controller uses this.
public class Navigator {

    private static final String FXML_FOLDER = "/com/ucc/parkingsystem/fxml/";

    // Opens any screen. "currentNode" is any control on the current screen
    // (we use it to find the window).
    public static void goTo(Node currentNode, String fxmlFile, String title,
                            double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(FXML_FOLDER + fxmlFile));
        Scene scene = new Scene(loader.load(), width, height);

        Stage stage = (Stage) currentNode.getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.sizeToScene();
        stage.centerOnScreen();
    }

    // Opens a screen ONLY if the logged-in user is an Admin.
    // Use this for every Admin-only screen.
    public static void goToAdminOnly(Node currentNode, String fxmlFile, String title,
                                     double width, double height) throws IOException {
        User user = Session.getCurrentUser();

        if (user == null || !user.isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Access denied. This screen is for Admin only.");
            alert.showAndWait();
            return;
        }
        goTo(currentNode, fxmlFile, title, width, height);
    }

    // Clears the login and returns to the login screen.
// Asks for confirmation, then clears the login and returns to the login screen.
    public static void logout(Node currentNode) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;   // Cancel was clicked, or the pop-up was closed: stay on the current screen
        }

        Session.logout();
        try {
            goTo(currentNode, "login-view.fxml", "Parking Management System - Login", 400, 350);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}