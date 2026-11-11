package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/**
 * Body of POST /api/rides/book. The rider comes from the JWT, never from the body.
 * Map pins are optional; with them the fare is distance-based.
 */
public record BookRideRequest(
        @NotBlank(message = "Pickup location is required")
        @Size(max = LOCATION_MAX, message = "Pickup location is too long")
        String pickup,

        @NotBlank(message = "Drop location is required")
        @Size(max = LOCATION_MAX, message = "Drop location is too long")
        String drop,

        @NotBlank(message = "Choose a valid vehicle type")
        @Pattern(regexp = VEHICLE_TYPE_PATTERN, message = "Choose a valid vehicle type")
        String vehicleType,

        @DecimalMin(value = "-90", message = "Invalid pickup pin") @DecimalMax(value = "90", message = "Invalid pickup pin")
        Double pickupLat,
        @DecimalMin(value = "-180", message = "Invalid pickup pin") @DecimalMax(value = "180", message = "Invalid pickup pin")
        Double pickupLng,
        @DecimalMin(value = "-90", message = "Invalid drop pin") @DecimalMax(value = "90", message = "Invalid drop pin")
        Double dropLat,
        @DecimalMin(value = "-180", message = "Invalid drop pin") @DecimalMax(value = "180", message = "Invalid drop pin")
        Double dropLng) implements RoutePoints {

    /** Booking without map pins (flat minimum fare). */
    public BookRideRequest(String pickup, String drop, String vehicleType) {
        this(pickup, drop, vehicleType, null, null, null, null);
    }
}
