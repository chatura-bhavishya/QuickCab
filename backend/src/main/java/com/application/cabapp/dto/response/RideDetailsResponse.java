package com.application.cabapp.dto.response;

/** A ride plus the assigned driver's details (driver is null until accepted). */
public record RideDetailsResponse(RideResponse ride, DriverResponse driver) {
}
