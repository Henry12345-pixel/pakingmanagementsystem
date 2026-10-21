package com.ucc.parkingsystem.util;

import java.util.Locale;
import java.util.regex.Pattern;

// The plate number template: 3 letters, a space, 4 numbers (e.g. "ABC 1234").
public final class PlateNumber {

    public static final String EXAMPLE = "ABC 1234";
    public static final String HINT = "3 letters, a space, then 4 numbers (e.g. " + EXAMPLE + ")";

    private static final int LETTERS = 3;
    private static final int DIGITS = 4;
    private static final Pattern VALID = Pattern.compile("^[A-Z]{3} [0-9]{4}$");

    private PlateNumber() { }

    // True only for a complete plate such as "ABC 1234".
    public static boolean isValid(String plate) {
        return plate != null && VALID.matcher(plate).matches();
    }

    // Cleans whatever was typed or pasted into the template as far as it goes:
    // upper-cases it, keeps letters for the first 3 slots and numbers for the last 4,
    // and puts the space in automatically. "abc1234" -> "ABC 1234", "ab1" -> "AB".
    public static String format(String raw) {
        if (raw == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        StringBuilder digits = new StringBuilder();

        for (char c : raw.toUpperCase(Locale.ROOT).toCharArray()) {
            if (letters.length() < LETTERS) {
                if (c >= 'A' && c <= 'Z') {
                    letters.append(c);
                }
            } else if (digits.length() < DIGITS) {
                if (c >= '0' && c <= '9') {
                    digits.append(c);
                }
            }
        }
        return digits.length() == 0 ? letters.toString() : letters + " " + digits;
    }
}
