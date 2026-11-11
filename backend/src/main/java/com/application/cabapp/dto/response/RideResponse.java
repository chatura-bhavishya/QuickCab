package com.application.cabapp.dto.response;

import java.time.LocalDateTime;

import com.application.cabapp.model.Ride;
import com.application.cabapp.model.RideStatus;

/**
 * Public view of a ride. The OTP is only included for the rider who booked
 * it: the rider reads it out to the driver to prove the right person was
 * picked up, so the driver must never receive it from the API.
 */
public record RideResponse(Long id, Long userId, Long driverId, String pickup, String drop,
                           String vehicleType, RideStatus status, String otp, boolean paid,
                           Double amount, boolean driverRated, LocalDateTime createdAt,
                           Double pickupLat, Double pickupLng, Double dropLat, Double dropLng,
                           Double distanceKm) {

    public static RideResponse forRider(Ride r) {
        return of(r, r.getOtp());
    }

    public static RideResponse forDriver(Ride r) {
        return of(r, null);
    }

    private static RideResponse of(Ride r, String otp) {
        return new RideResponse(r.getId(), r.getUserId(), r.getDriverId(), r.getPickup(), r.getDrop(),
                r.getVehicleType(), r.getStatus(), otp, r.isPaid(), r.getAmount(), r.isDriverRated(),
                r.getCreatedAt(), r.getPickupLat(), r.getPickupLng(), r.getDropLat(), r.getDropLng(),
                r.getDistanceKm());
    }
}
