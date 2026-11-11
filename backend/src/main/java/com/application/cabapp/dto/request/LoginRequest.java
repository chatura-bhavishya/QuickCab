package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of the user and driver login endpoints. */
public record LoginRequest(
        @NotBlank(message = "Username is required")
        @Size(max = USERNAME_MAX, message = "Invalid username or password")
        String username,

        @NotBlank(message = "Password is required")
        @Size(max = PASSWORD_MAX, message = "Invalid username or password")
        String password) {
}
