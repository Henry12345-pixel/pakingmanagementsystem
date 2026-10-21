package com.ucc.parkingsystem.util;

import java.util.ArrayList;
import java.util.List;

// Rules for staff passwords. Used when creating an account and when changing a password.
// (Login does NOT use this, so older accounts with simpler passwords can still sign in.)
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    public static final String REQUIREMENTS =
            "At least 8 characters, with an uppercase letter, a number, and a special character (e.g. ! @ # $ %).";

    private PasswordPolicy() { }

    // Returns what the password is still missing. An empty list means it is valid.
    public static List<String> missing(String password) {
        String pw = password == null ? "" : password;
        List<String> missing = new ArrayList<>();

        if (pw.length() < MIN_LENGTH) {
            missing.add("at least " + MIN_LENGTH + " characters");
        }
        if (pw.chars().noneMatch(Character::isUpperCase)) {
            missing.add("an uppercase letter");
        }
        if (pw.chars().noneMatch(Character::isDigit)) {
            missing.add("a number");
        }
        if (pw.chars().noneMatch(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))) {
            missing.add("a special character");
        }
        if (pw.chars().anyMatch(Character::isWhitespace)) {
            missing.add("no spaces");
        }
        return missing;
    }

    public static boolean isValid(String password) {
        return missing(password).isEmpty();
    }

    // Human-readable version of missing(), e.g. "Still needed: an uppercase letter, a number."
    public static String describeMissing(String password) {
        List<String> missing = missing(password);
        return missing.isEmpty() ? "" : "Still needed: " + String.join(", ", missing) + ".";
    }
}
