package com.application.cabapp.model;

import java.util.Set;

/** Supported vehicle types (stored as lowercase strings). */
public final class VehicleType {
    public static final String CAR = "car";
    public static final String BIKE = "bike";
    public static final String AUTO = "auto";

    public static final Set<String> ALL = Set.of(CAR, BIKE, AUTO);

    private VehicleType() {}
}
