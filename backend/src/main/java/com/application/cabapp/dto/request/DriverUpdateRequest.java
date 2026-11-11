package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of PUT /api/drivers/me. A blank password keeps the current one. */
public record DriverUpdateRequest(
        @NotBlank(message = "Contact number is required")
        @Pattern(regexp = PHONE_PATTERN, message = "Enter a valid contact number")
        String contact,

        @NotBlank(message = "Choose a valid vehicle type")
        @Pattern(regexp = VEHICLE_TYPE_PATTERN, message = "Choose a valid vehicle type")
        String vehicleType,

        @NotBlank(message = "Vehicle number is required")
        @Size(max = VEHICLE_NUMBER_MAX, message = "Enter a valid vehicle number")
        @Pattern(regexp = VEHICLE_NUMBER_PATTERN, message = "Enter a valid vehicle number")
        String vehicleNumber,

        @Pattern(regexp = OPTIONAL_PASSWORD_PATTERN, message = "Password must be 6-72 characters")
        String password) {
}
