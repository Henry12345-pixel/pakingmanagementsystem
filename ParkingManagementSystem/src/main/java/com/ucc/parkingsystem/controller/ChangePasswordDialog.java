
package com.ucc.parkingsystem.controller;

import com.ucc.parkingsystem.model.User;
import com.ucc.parkingsystem.util.PasswordPolicy;
import com.ucc.parkingsystem.util.Ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.stage.Window;

import java.util.Optional;

public final class ChangePasswordDialog {

    private ChangePasswordDialog() {
    }

    public static Optional<String> show(Window owner, User staff) {

        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Change password");
        dialog.setHeaderText(
                "Set a new password for " + staff.getFullName()
                        + " (" + staff.getUsername() + ")"
        );

        Navigator.styleDialog(dialog.getDialogPane());

        ButtonType changeButtonType = new ButtonType(
                "Change password",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                changeButtonType,
                ButtonType.CANCEL
        );

        PasswordField newHidden = new PasswordField();
        TextField newShown = new TextField();

        PasswordField confirmHidden = new PasswordField();
        TextField confirmShown = new TextField();

        newHidden.setPromptText("New password");
        newShown.setPromptText("New password");

        confirmHidden.setPromptText("Re-type the new password");
        confirmShown.setPromptText("Re-type the new password");

        newShown.textProperty().bindBidirectional(
                newHidden.textProperty()
        );

        confirmShown.textProperty().bindBidirectional(
                confirmHidden.textProperty()
        );

        StackPane newPasswordBox = createPasswordBox(
                newHidden,
                newShown
        );

        StackPane confirmPasswordBox = createPasswordBox(
                confirmHidden,
                confirmShown
        );

        Label newLabel = new Label("New password");
        newLabel.getStyleClass().add("field-label");

        Label confirmLabel = new Label("Confirm password");
        confirmLabel.getStyleClass().add("field-label");

        Label hint = new Label();
        hint.setWrapText(true);
        hint.setMinHeight(Region.USE_PREF_SIZE);

        Ui.muted(hint, PasswordPolicy.REQUIREMENTS);

        VBox content = new VBox(
                12,
                newLabel,
                newPasswordBox,
                confirmLabel,
                confirmPasswordBox,
                hint
        );

        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);

        Node changeButton = dialog.getDialogPane()
                .lookupButton(changeButtonType);

        changeButton.setDisable(true);

        Runnable refresh = () -> {

            String password = newHidden.getText();
            String confirm = confirmHidden.getText();

            boolean valid = PasswordPolicy.isValid(password);
            boolean matches = password.equals(confirm);

            if (password.isEmpty()) {
                Ui.muted(hint, PasswordPolicy.REQUIREMENTS);

            } else if (!valid) {
                Ui.error(
                        hint,
                        PasswordPolicy.describeMissing(password)
                );

            } else if (!matches) {
                Ui.error(
                        hint,
                        "The two passwords do not match."
                );

            } else {
                Ui.success(hint, "Looks good.");
            }

            changeButton.setDisable(!(valid && matches));
        };

        newHidden.textProperty().addListener(
                (obs, oldText, newText) -> refresh.run()
        );

        confirmHidden.textProperty().addListener(
                (obs, oldText, newText) -> refresh.run()
        );

        dialog.setResultConverter(button ->
                button == changeButtonType
                        ? newHidden.getText()
                        : null
        );

        Platform.runLater(newHidden::requestFocus);

        return dialog.showAndWait();
    }

    private static StackPane createPasswordBox(
            PasswordField hidden,
            TextField shown) {

        hidden.setMaxWidth(Double.MAX_VALUE);
        shown.setMaxWidth(Double.MAX_VALUE);

        hidden.getStyleClass().add("password-with-eye");
        shown.getStyleClass().add("password-with-eye");

        shown.setVisible(false);
        shown.setManaged(false);

        SVGPath eyeIcon = new SVGPath();
        eyeIcon.getStyleClass().add("password-eye-icon");

        updateEyeIcon(eyeIcon, false);

        Button eyeButton = new Button();
        eyeButton.getStyleClass().add("password-eye-button");
        eyeButton.setGraphic(eyeIcon);
        eyeButton.setFocusTraversable(false);
        eyeButton.setAccessibleText("Show password");

        eyeButton.setOnAction(event -> {

            boolean show = !shown.isVisible();

            hidden.setVisible(!show);
            hidden.setManaged(!show);

            shown.setVisible(show);
            shown.setManaged(show);

            updateEyeIcon(eyeIcon, show);

            eyeButton.setAccessibleText(
                    show ? "Hide password" : "Show password"
            );

            if (show) {
                shown.requestFocus();
                shown.positionCaret(
                        shown.getText().length()
                );

            } else {
                hidden.requestFocus();
                hidden.positionCaret(
                        hidden.getText().length()
                );
            }
        });

        StackPane box = new StackPane(
                hidden,
                shown,
                eyeButton
        );

        StackPane.setAlignment(
                eyeButton,
                Pos.CENTER_RIGHT
        );

        StackPane.setMargin(
                eyeButton,
                new Insets(0, 9, 0, 0)
        );

        return box;
    }

    private static void updateEyeIcon(
            SVGPath icon,
            boolean visible) {

        String eye =
                "M1 9 C4 3 12 3 15 9 " +
                        "C12 15 4 15 1 9 Z " +
                        "M8 6 A3 3 0 1 0 8 12 " +
                        "A3 3 0 1 0 8 6 Z";

        icon.setContent(
                visible ? eye + " M1 1 L15 15" : eye
        );
    }
}
