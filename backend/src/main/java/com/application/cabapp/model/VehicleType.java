package com.application.cabapp.model;

/**
 * Supported vehicle types (stored as lowercase strings). Request validation
 * uses ValidationRules.VEHICLE_TYPE_PATTERN, which must list the same values.
 */
public final class VehicleType {
    public static final String CAR = "car";
    public static final String BIKE = "bike";
    public static final String AUTO = "auto";

    private VehicleType() {}
}
