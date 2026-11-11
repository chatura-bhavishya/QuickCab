package com.application.cabapp.dto.request;

import static com.application.cabapp.validation.ValidationRules.*;

import jakarta.validation.constraints.*;

/** Body of POST /api/rides/{id}/start. */
public record StartRideRequest(
        @NotBlank(message = "Invalid OTP")
        @Pattern(regexp = OTP_PATTERN, message = "Invalid OTP")
        String otp) {
}
