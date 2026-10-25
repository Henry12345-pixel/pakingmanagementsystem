package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

// Changes the screen shown in the window. Every controller uses this.
public class Navigator {

    private static final String FXML_FOLDER = "/com/ucc/parkingsystem/fxml/";
    private static final String STYLESHEET = "/com/ucc/parkingsystem/app.css";

    // Opens any screen in the SAME window, replacing the current scene.
    // Preserves maximized state across the switch (Login <-> Admin <-> Staff).
    public static void goTo(Node currentNode, String fxmlFile, String title,
                            double width, double height) throws IOException {
        Stage stage = (Stage) currentNode.getScene().getWindow();
        boolean wasMaximized = stage.isMaximized();

        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(FXML_FOLDER + fxmlFile));
        Scene scene = new Scene(loader.load(), width, height);
        scene.getStylesheets().add(Navigator.class.getResource(STYLESHEET).toExternalForm());

        stage.setTitle(title);
        stage.setScene(scene);

        applyWindowState(stage, wasMaximized);
    }

    // Opens a screen ONLY if the logged-in user is an Admin.
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

    // Opens an FXML file as a separate modal popup window, blocking until it's closed.
    // The popup itself is a plain dialog window, not a maximized app screen,
    // so it is NOT affected by this maximize logic.
    public static void openPopup(Node ownerNode, String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(FXML_FOLDER + fxmlFile));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(Navigator.class.getResource(STYLESHEET).toExternalForm());

        Stage popup = new Stage();
        popup.initOwner(ownerNode.getScene().getWindow());
        popup.initModality(Modality.WINDOW_MODAL);
        popup.setTitle(title);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // Asks for confirmation, then clears the login and returns to the login screen,
    // keeping the window maximized.
    public static void logout(Node currentNode) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to log out?");
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.OK) {
            return;
        }

        Session.logout();
        try {
            // Always force maximized on logout, regardless of prior state,
            // so the login screen never appears shrunk.
            Stage stage = (Stage) currentNode.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(
                    Navigator.class.getResource(FXML_FOLDER + "login-view.fxml"));
            Scene scene = new Scene(loader.load(), 400, 450);
            scene.getStylesheets().add(Navigator.class.getResource(STYLESHEET).toExternalForm());

            stage.setTitle("Parking Management System - Login");
            stage.setScene(scene);

            applyWindowState(stage, true);   // force maximized on logout, every time

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Applies the app's stylesheet to a dialog's content pane, so popups match the app's look.
    public static void styleDialog(DialogPane dialogPane) {
        dialogPane.getStylesheets().add(Navigator.class.getResource(STYLESHEET).toExternalForm());
    }

    // Central place that actually sets the maximize state after a scene change.
    // Uses Platform.runLater because setMaximized(true) applied in the SAME pulse
    // as setScene(...) can be silently overridden by the window manager on Windows;
    // deferring it one pulse makes it stick reliably every time.
    private static void applyWindowState(Stage stage, boolean maximized) {
        if (maximized) {
            stage.setMaximized(false);   // force a real state change so the re-set below isn't a no-op
            Platform.runLater(() -> stage.setMaximized(true));
        } else {
            stage.setMaximized(false);
            stage.sizeToScene();
            stage.centerOnScreen();
        }
    }
}