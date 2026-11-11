package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/rides/{id}/rate. */
public record RateRideRequest(
        @NotNull(message = "Rating must be between 1 and 5")
        @DecimalMin(value = RATING_MIN, message = "Rating must be between 1 and 5")
        @DecimalMax(value = RATING_MAX, message = "Rating must be between 1 and 5")
        Double rating) {
}
