package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.Session;
import com.ucc.parkingsystem.model.User;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
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

    // Opens any screen. "currentNode" is any control on the current screen
    // (we use it to find the window).
    //
    // Every screen opens full screen. minWidth/minHeight are the smallest size the
    // user can shrink the window to; layouts resize freely between that and full screen.
    public static void goTo(Node currentNode, String fxmlFile, String title,
                            double minWidth, double minHeight) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(FXML_FOLDER + fxmlFile));
        Parent root = loader.load();

        Stage stage = (Stage) currentNode.getScene().getWindow();

        // Keep the current window size so the swap doesn't flash a smaller window.
        double width = stage.getWidth() > 0 ? stage.getWidth() : minWidth;
        double height = stage.getHeight() > 0 ? stage.getHeight() : minHeight;

        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(stylesheet());

        stage.setTitle(title);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setScene(scene);
        stage.setMaximized(true);
    }

    // Opens a screen as a pop-up window in the centre of the current window.
    // It is modal: the screen behind it is locked until the pop-up is closed,
    // and this method returns only after that.
    public static void openPopup(Node ownerNode, String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(FXML_FOLDER + fxmlFile));
        Parent root = loader.load();

        Stage owner = (Stage) ownerNode.getScene().getWindow();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(stylesheet());

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.WINDOW_MODAL);
        popup.setTitle(title);
        popup.setScene(scene);
        popup.setResizable(false);
        popup.setOnShown(event -> {
            popup.setX(owner.getX() + (owner.getWidth() - popup.getWidth()) / 2);
            popup.setY(owner.getY() + (owner.getHeight() - popup.getHeight()) / 2);
        });
        popup.showAndWait();
    }

    // Opens a screen ONLY if the logged-in user is an Admin.
    // Use this for every Admin-only screen.
    public static void goToAdminOnly(Node currentNode, String fxmlFile, String title,
                                     double minWidth, double minHeight) throws IOException {
        User user = Session.getCurrentUser();

        if (user == null || !user.isAdmin()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,
                    "Access denied. This screen is for Admin only.");
            alert.showAndWait();
            return;
        }
        goTo(currentNode, fxmlFile, title, minWidth, minHeight);
    }

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
    }

    // Gives a pop-up dialog the same look as the rest of the app.
    public static void styleDialog(DialogPane pane) {
        pane.getStylesheets().add(stylesheet());
    }

    private static String stylesheet() {
        return Navigator.class.getResource(STYLESHEET).toExternalForm();
    }
}