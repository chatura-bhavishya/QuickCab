package com.application.cabapp.validation;

/**
 * Validation limits and patterns shared by the request DTOs. They are
 * compile-time constants so they can be used inside annotations. The
 * frontend mirrors these rules in src/utils/validation.js.
 */
public final class ValidationRules {

    public static final int USERNAME_MIN = 3;
    public static final int USERNAME_MAX = 30;
    public static final String USERNAME_PATTERN = "^[A-Za-z0-9_.-]+$";

    public static final int PASSWORD_MIN = 6;
    /** BCrypt only uses the first 72 bytes of a password. */
    public static final int PASSWORD_MAX = 72;
    /** Empty (keep current password) or a valid new password. */
    public static final String OPTIONAL_PASSWORD_PATTERN = "^$|^.{" + PASSWORD_MIN + "," + PASSWORD_MAX + "}$";

    public static final int EMAIL_MAX = 254;

    /** Optional +, 7-15 digits, spaces or dashes allowed between digits. */
    public static final String PHONE_PATTERN = "^(?=(?:\\D*\\d){7,15}\\D*$)\\+?[0-9][0-9 -]*[0-9]$";

    public static final String VEHICLE_TYPE_PATTERN = "^(car|bike|auto)$";
    public static final int VEHICLE_NUMBER_MAX = 20;
    public static final String VEHICLE_NUMBER_PATTERN = "^[A-Za-z0-9 -]+$";

    public static final int LOCATION_MAX = 200;
    /** Matches the default VARCHAR(255) column created by Hibernate. */
    public static final int CHAT_MESSAGE_MAX = 255;
    public static final String OTP_PATTERN = "^\\d{4}$";

    public static final String RATING_MIN = "1";
    public static final String RATING_MAX = "5";

    private ValidationRules() {}
}
