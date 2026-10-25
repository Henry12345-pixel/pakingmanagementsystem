package com.ucc.parkingsystem;

import com.ucc.parkingsystem.database.DatabaseSetup;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        DatabaseSetup.initialize();

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("fxml/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 450);
        scene.getStylesheets().add(HelloApplication.class.getResource("app.css").toExternalForm());

        // Load the icon defensively: if logo.png is ever missing, the app still starts
        // instead of crashing with a NullPointerException.
        InputStream iconStream = HelloApplication.class.getResourceAsStream("logo.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }

        stage.setTitle("Parking Management System - Login");
        stage.setScene(scene);

        // Same deferred-maximize trick as Navigator.applyWindowState, so the very
        // first window on launch maximizes reliably too.
        stage.setMaximized(false);
        stage.show();
        Platform.runLater(() -> stage.setMaximized(true));
    }
}