package com.application.cabapp.dto.response;

import com.application.cabapp.model.Driver;

/** Public view of a driver (no password). */
public record DriverResponse(Long id, String username, String contact, String vehicleType,
                             String vehicleNumber, Double rating, Integer ratingCount) {

    public static DriverResponse from(Driver d) {
        return new DriverResponse(d.getId(), d.getUsername(), d.getContact(), d.getVehicleType(),
                d.getVehicleNumber(), d.getRating(), d.getRatingCount());
    }
}
