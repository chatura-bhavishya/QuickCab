package com.application.cabapp.service;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.application.cabapp.model.VehicleType;

/**
 * Works out the fare for a trip.
 *
 * With map pins: base fare + per-km rate, but never less than the vehicle's
 * minimum. Without pins we can't measure anything, so the minimum applies
 * (this is also what every ride cost before the map existed).
 *
 * Distance is the straight line between the two pins (haversine). Real roads
 * are longer, so it slightly undercharges; a routing API would fix that.
 */
@Component
public class FareCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;
    /** Anything longer is almost certainly a mis-placed pin. */
    public static final double MAX_TRIP_KM = 300.0;

    record Rate(double base, double perKm, double minimum) {}

    // INR. Minimums equal the old flat fares.
    private static final Map<String, Rate> RATES = Map.of(
            VehicleType.BIKE, new Rate(20, 6, 60),
            VehicleType.AUTO, new Rate(30, 10, 90),
            VehicleType.CAR, new Rate(50, 15, 150));

    public double minimumFare(String vehicleType) {
        return rate(vehicleType).minimum();
    }

    public double fareFor(String vehicleType, double distanceKm) {
        Rate rate = rate(vehicleType);
        double fare = rate.base() + rate.perKm() * distanceKm;
        return Math.round(Math.max(fare, rate.minimum()));
    }

    /** Great-circle distance in km, rounded to one decimal. */
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double km = 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
        return Math.round(km * 10.0) / 10.0;
    }

    private static Rate rate(String vehicleType) {
        Rate rate = RATES.get(vehicleType);
        if (rate == null) throw new IllegalArgumentException("Unknown vehicle type: " + vehicleType);
        return rate;
    }
}
