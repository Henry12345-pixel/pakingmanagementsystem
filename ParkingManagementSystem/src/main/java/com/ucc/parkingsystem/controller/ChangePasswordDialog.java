package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.User;
import com.ucc.parkingsystem.util.PasswordPolicy;
import com.ucc.parkingsystem.util.Ui;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.shape.SVGPath;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Optional;

// The pop-up opened by the "Change Password" button on a staff row.
// The button stays disabled until the new password meets PasswordPolicy and both boxes match.
public final class ChangePasswordDialog {

    private ChangePasswordDialog() { }

    // Returns the new (plain) password, or empty if the admin cancelled.
    public static Optional<String> show(Window owner, User staff) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Change password");
        dialog.setHeaderText("Set a new password for " + staff.getFullName()
                + " (" + staff.getUsername() + ")");
        Navigator.styleDialog(dialog.getDialogPane());

        ButtonType changeButtonType = new ButtonType("Change password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        PasswordField newHidden = new PasswordField();
        TextField newShown = new TextField();
        PasswordField confirmHidden = new PasswordField();
        TextField confirmShown = new TextField();

        newHidden.setPromptText("New password");
        newShown.setPromptText("New password");
        confirmHidden.setPromptText("Re-type the new password");
        confirmShown.setPromptText("Re-type the new password");

        // Keep the hidden and visible boxes in sync, same trick as the login screen.
        newShown.textProperty().bindBidirectional(newHidden.textProperty());
        confirmShown.textProperty().bindBidirectional(confirmHidden.textProperty());
        setShown(newHidden, newShown, false);
        setShown(confirmHidden, confirmShown, false);

        StackPane newBox = passwordBox(newHidden, newShown);
        StackPane confirmBox = passwordBox(confirmHidden, confirmShown);

        Label hint = new Label();
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE);
        Ui.muted(hint, PasswordPolicy.REQUIREMENTS);

        Label newLabel = new Label("New password");
        newLabel.getStyleClass().add("field-label");
        Label confirmLabel = new Label("Confirm password");
        confirmLabel.getStyleClass().add("field-label");

        VBox content = new VBox(12,
                newLabel, newBox,
                confirmLabel, confirmBox,
                hint);
        content.setPrefWidth(460);
        dialog.getDialogPane().setContent(content);

        Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);

        Runnable refresh = () -> {
            String password = newHidden.getText();
            String confirm = confirmHidden.getText();
            boolean valid = PasswordPolicy.isValid(password);
            boolean matches = password.equals(confirm);

            if (password.isEmpty()) {
                Ui.muted(hint, PasswordPolicy.REQUIREMENTS);
            } else if (!valid) {
                Ui.error(hint, PasswordPolicy.describeMissing(password));
            } else if (!matches) {
                Ui.error(hint, "The two passwords do not match.");
            } else {
                Ui.success(hint, "Looks good.");
            }
            changeButton.setDisable(!(valid && matches));
        };
        newHidden.textProperty().addListener((obs, oldText, newText) -> refresh.run());
        confirmHidden.textProperty().addListener((obs, oldText, newText) -> refresh.run());

        dialog.setResultConverter(button -> button == changeButtonType ? newHidden.getText() : null);
        Platform.runLater(newHidden::requestFocus);

        return dialog.showAndWait();
    }

    private static void setShown(PasswordField hidden, TextField shown, boolean showNow) {
        hidden.setVisible(!showNow);
        hidden.setManaged(!showNow);
        shown.setVisible(showNow);
        shown.setManaged(showNow);
    }

    private static StackPane passwordBox(PasswordField hidden, TextField shown) {
        hidden.setMaxWidth(Double.MAX_VALUE);
        shown.setMaxWidth(Double.MAX_VALUE);
        hidden.getStyleClass().add("password-with-eye");
        shown.getStyleClass().add("password-with-eye");
        SVGPath icon = new SVGPath();
        icon.getStyleClass().add("password-eye-icon");
        icon.setContent("M1 9 C4 3 12 3 15 9 C12 15 4 15 1 9 Z M8 6 A3 3 0 1 0 8 12 A3 3 0 1 0 8 6 Z");
        Button toggle = new Button();
        toggle.getStyleClass().add("password-eye-button");
        toggle.setGraphic(icon);
        toggle.setFocusTraversable(false);
        toggle.setAccessibleText("Show password");
        toggle.setOnAction(e -> {
            boolean show = !shown.isVisible();
            setShown(hidden, shown, show);
            icon.setContent("M1 9 C4 3 12 3 15 9 C12 15 4 15 1 9 Z M8 6 A3 3 0 1 0 8 12 A3 3 0 1 0 8 6 Z" + (show ? " M1 1 L15 15" : ""));
            toggle.setAccessibleText(show ? "Hide password" : "Show password");
            TextField focus = show ? shown : hidden;
            focus.requestFocus();
            focus.positionCaret(focus.getText().length());
        });
        StackPane box = new StackPane(hidden, shown, toggle);
        StackPane.setAlignment(toggle, Pos.CENTER_RIGHT);
        StackPane.setMargin(toggle, new Insets(0, 9, 0, 0));
        return box;
    }

}
