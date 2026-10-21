package com.ucc.parkingsystem.util;

import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

// Small helpers shared by every screen.
public final class Ui {

    private static final List<String> MESSAGE_CLASSES =
            List.of("message-error", "message-success", "message-muted");

    private Ui() { }

    // Message labels get their colour from app.css classes instead of inline styles.
    public static void error(Label label, String text) {
        show(label, text, "message-error");
    }

    public static void success(Label label, String text) {
        show(label, text, "message-success");
    }

    public static void muted(Label label, String text) {
        show(label, text, "message-muted");
    }

    private static void show(Label label, String text, String styleClass) {
        label.getStyleClass().removeAll(MESSAGE_CLASSES);
        label.getStyleClass().add(styleClass);
        label.setText(text);
    }

    // "GROUND" -> "Ground". Null-safe (the old code crashed on a null floor).
    public static String titleCase(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT)
                + value.substring(1).toLowerCase(Locale.ROOT);
    }

    // Lets a dropdown keep the real value ("GROUND") but display "Ground".
    public static StringConverter<String> titleCaseConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(String value) {
                return titleCase(value);
            }

            @Override
            public String fromString(String text) {
                return text;
            }
        };
    }
}
