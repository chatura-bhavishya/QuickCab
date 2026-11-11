package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of PUT /api/users/me. A blank password keeps the current one. */
public record UserUpdateRequest(
        @NotBlank(message = "Email is required")
        @Size(max = EMAIL_MAX, message = "Enter a valid email address")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = PHONE_PATTERN, message = "Enter a valid contact number")
        String contact,

        @Pattern(regexp = OPTIONAL_PASSWORD_PATTERN, message = "Password must be 6-72 characters")
        String password) {
}
