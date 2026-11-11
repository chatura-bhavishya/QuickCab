package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/rides/book. The rider is taken from the JWT, never from the body. */
public record BookRideRequest(
        @NotBlank(message = "Pickup location is required")
        @Size(max = LOCATION_MAX, message = "Pickup location is too long")
        String pickup,

        @NotBlank(message = "Drop location is required")
        @Size(max = LOCATION_MAX, message = "Drop location is too long")
        String drop,

        @NotBlank(message = "Choose a valid vehicle type")
        @Pattern(regexp = VEHICLE_TYPE_PATTERN, message = "Choose a valid vehicle type")
        String vehicleType) {
}
