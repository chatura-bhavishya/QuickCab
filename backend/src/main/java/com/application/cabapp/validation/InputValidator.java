package com.application.cabapp.validation;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import com.application.cabapp.model.VehicleType;

/**
 * Server-side input validation. Each method returns a user-friendly error
 * message, or {@code null} when the value is valid. Limits mirror the
 * frontend rules in src/utils/validation.js.
 */
public final class InputValidator {

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 30;
    public static final int PASSWORD_MIN = 6;
    /** BCrypt only uses the first 72 bytes of a password. */
    public static final int PASSWORD_MAX_BYTES = 72;
    public static final int EMAIL_MAX = 254;
    public static final int PHONE_DIGITS_MIN = 7;
    public static final int PHONE_DIGITS_MAX = 15;
    public static final int VEHICLE_NUMBER_MAX = 20;
    public static final int LOCATION_MAX = 200;
    /** Matches the default VARCHAR(255) column created by Hibernate. */
    public static final int CHAT_MESSAGE_MAX = 255;
    public static final double RATING_MIN = 1;
    public static final double RATING_MAX = 5;

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.-]+$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    /** Optional leading +, digits, with spaces or dashes allowed as separators. */
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9][0-9 -]*[0-9]$");
    private static final Pattern VEHICLE_NUMBER = Pattern.compile("^[A-Za-z0-9 -]+$");

    private InputValidator() {}

    public static String username(String value) {
        if (isBlank(value)) return "Username is required";
        if (value.length() < USERNAME_MIN || value.length() > USERNAME_MAX)
            return "Username must be " + USERNAME_MIN + "-" + USERNAME_MAX + " characters";
        if (!USERNAME.matcher(value).matches())
            return "Username may only contain letters, numbers, dots, dashes and underscores";
        return null;
    }

    public static String password(String value) {
        if (isBlank(value)) return "Password is required";
        if (value.length() < PASSWORD_MIN) return "Password must be at least " + PASSWORD_MIN + " characters";
        if (value.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX_BYTES) return "Password is too long";
        return null;
    }

    /** For profile updates: blank means "keep the current password". */
    public static String optionalPassword(String value) {
        return isBlank(value) ? null : password(value);
    }

    public static String email(String value) {
        if (isBlank(value)) return "Email is required";
        if (value.length() > EMAIL_MAX || !EMAIL.matcher(value).matches()) return "Enter a valid email address";
        return null;
    }

    public static String contact(String value) {
        if (isBlank(value)) return "Contact number is required";
        int digits = value.replaceAll("[^0-9]", "").length();
        if (!PHONE.matcher(value).matches() || digits < PHONE_DIGITS_MIN || digits > PHONE_DIGITS_MAX)
            return "Enter a valid contact number";
        return null;
    }

    public static String vehicleType(String value) {
        if (value == null || !VehicleType.ALL.contains(value)) return "Choose a valid vehicle type";
        return null;
    }

    public static String vehicleNumber(String value) {
        if (isBlank(value)) return "Vehicle number is required";
        if (value.length() > VEHICLE_NUMBER_MAX || !VEHICLE_NUMBER.matcher(value).matches())
            return "Enter a valid vehicle number";
        return null;
    }

    public static String location(String value, String label) {
        if (isBlank(value)) return label + " is required";
        if (value.length() > LOCATION_MAX) return label + " is too long";
        return null;
    }

    public static String chatMessage(String value) {
        if (isBlank(value)) return "Message cannot be empty";
        if (value.length() > CHAT_MESSAGE_MAX) return "Message is too long";
        return null;
    }

    public static String rating(Double value) {
        if (value == null || value < RATING_MIN || value > RATING_MAX) return "Rating must be between 1 and 5";
        return null;
    }

    /** Returns the first non-null error, or null if all checks passed. */
    public static String firstError(String... errors) {
        for (String e : errors) {
            if (e != null) return e;
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
