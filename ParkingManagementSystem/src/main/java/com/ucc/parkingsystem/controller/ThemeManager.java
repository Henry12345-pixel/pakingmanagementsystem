package com.ucc.parkingsystem.controller;

import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.prefs.Preferences;

/** Shared appearance for all screens and dialogs. No database changes. */
public final class ThemeManager {
    private static final Preferences PREFS = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean light = PREFS.getBoolean("lightTheme", false);
    private static final String CSS = ThemeManager.class.getResource("/com/ucc/parkingsystem/app.css").toExternalForm();
    private ThemeManager() {}

    public static void install(Scene scene) {
        if (!scene.getStylesheets().contains(CSS)) scene.getStylesheets().add(CSS);
        apply(scene.getRoot());
        // Put the switch in the existing top header on each screen, and in login form.
        if (scene.getRoot() instanceof BorderPane root && root.getTop() instanceof HBox header) {
            Button toggle = makeToggle(scene);
            int logoutIndex = header.getChildren().size();
            for (int i = 0; i < header.getChildren().size(); i++) {
                if (header.getChildren().get(i) instanceof Button b && b.getText().equalsIgnoreCase("Logout")) {
                    logoutIndex = i;
                    break;
                }
            }
            header.getChildren().add(logoutIndex, toggle);
            // Add logo to every page header, not just dashboards.
            ImageView logo = logo(34);
            header.getChildren().add(0, logo);
        } else if (scene.getRoot() instanceof HBox loginRoot) {
            Button toggle = makeToggle(scene);
            toggle.getStyleClass().add("login-theme-toggle");
            // Login hero and form remain unchanged; overlay the toggle at upper-right.
            if (loginRoot.getChildren().size() > 1 && loginRoot.getChildren().get(1) instanceof StackPane form) {
                form.getChildren().add(toggle);
                StackPane.setAlignment(toggle, javafx.geometry.Pos.TOP_RIGHT);
                StackPane.setMargin(toggle, new javafx.geometry.Insets(18));
            }
        }
    }

    private static ImageView logo(double size) {
        ImageView view = new ImageView(new Image(ThemeManager.class.getResourceAsStream("/com/ucc/parkingsystem/logo.png")));
        view.setFitHeight(size);
        view.setFitWidth(size);
        view.setPreserveRatio(true);
        return view;
    }

    private static Button makeToggle(Scene scene) {
        Button button = new Button(light ? "☾ Dark mode" : "☀ Light mode");
        button.getStyleClass().addAll("button-ghost", "theme-toggle");
        button.setOnAction(event -> {
            light = !light;
            PREFS.putBoolean("lightTheme", light);
            apply(scene.getRoot());
            button.setText(light ? "☾ Dark mode" : "☀ Light mode");
        });
        return button;
    }

    public static void apply(Node node) {
        node.getStyleClass().removeAll("theme-light", "theme-dark");
        node.getStyleClass().add(light ? "theme-light" : "theme-dark");
    }

    public static void styleDialog(DialogPane pane) {
        if (!pane.getStylesheets().contains(CSS)) pane.getStylesheets().add(CSS);
        apply(pane);
    }
}
