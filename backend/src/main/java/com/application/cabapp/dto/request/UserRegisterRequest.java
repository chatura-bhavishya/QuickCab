package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/users/register. */
public record UserRegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = USERNAME_MIN, max = USERNAME_MAX, message = "Username must be 3-30 characters")
        @Pattern(regexp = USERNAME_PATTERN, message = "Username may only contain letters, numbers, dots, dashes and underscores")
        String username,

        @NotBlank(message = "Email is required")
        @Size(max = EMAIL_MAX, message = "Enter a valid email address")
        @Email(message = "Enter a valid email address")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = PASSWORD_MIN, max = PASSWORD_MAX, message = "Password must be 6-72 characters")
        String password,

        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = PHONE_PATTERN, message = "Enter a valid contact number")
        String contact) {
}
