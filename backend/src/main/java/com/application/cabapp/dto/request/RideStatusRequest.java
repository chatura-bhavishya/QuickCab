package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/rides/{id}/status. Only these two steps go through this endpoint. */
public record RideStatusRequest(
        @NotBlank(message = "Invalid status")
        @Pattern(regexp = "^(ON_THE_WAY|ARRIVED)$", message = "Invalid status")
        String status) {
}
