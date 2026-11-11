package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/rides/estimate: price a trip before booking it. */
public record FareEstimateRequest(
        @NotBlank(message = "Choose a valid vehicle type")
        @Pattern(regexp = VEHICLE_TYPE_PATTERN, message = "Choose a valid vehicle type")
        String vehicleType,

        @NotNull(message = "Set the pickup pin") @DecimalMin("-90") @DecimalMax("90") Double pickupLat,
        @NotNull(message = "Set the pickup pin") @DecimalMin("-180") @DecimalMax("180") Double pickupLng,
        @NotNull(message = "Set the drop pin") @DecimalMin("-90") @DecimalMax("90") Double dropLat,
        @NotNull(message = "Set the drop pin") @DecimalMin("-180") @DecimalMax("180") Double dropLng)
        implements RoutePoints {
}
